package com.example.test_for_diplom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_for_diplom.databinding.AiChatFragmentBinding
import com.example.test_for_diplom.databinding.ItemMessageReceivedBinding
import com.example.test_for_diplom.databinding.ItemMessageSentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient

// Data class for a chat message
data class Message(val text: String, val isSent: Boolean)

// Data class for the request sent to the server
data class MessageRequest(val message: String)

// Data class for the response received from the server
data class EmotionResponse(
    val message: String,
    val emotion: String,
    val confidence: Float
)

// API service interface for Retrofit
interface ApiService {
    @POST("process")
    fun processMessage(@Body request: MessageRequest): Call<EmotionResponse>
}

// MessageAdapter (unchanged from your original code)
class MessageAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSent) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
        }
    }
}

// AiChatFragment with server integration
class AiChatFragment : Fragment() {

    private var _binding: AiChatFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AiChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupRetrofit()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)
        binding.chatRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // New messages appear at the bottom
            }
        }
    }

    private fun setupRetrofit() {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .apply {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
                val sslContext = SSLContext.getInstance("SSL").apply {
                    init(null, trustAllCerts, SecureRandom())
                }
                sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                hostnameVerifier { _, _ -> true }
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://192.168.0.71:5001/") // Updated IP for physical device
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Add the sent message to the chat
                val sentMessage = Message(messageText, isSent = true)
                messageAdapter.addMessage(sentMessage)
                binding.messageEditText.text?.clear()

                // Send the message to the server
                val request = MessageRequest(messageText)
                apiService.processMessage(request).enqueue(object : Callback<EmotionResponse> {
                    override fun onResponse(call: Call<EmotionResponse>, response: Response<EmotionResponse>) {
                        if (response.isSuccessful) {
                            val emotionResponse = response.body()
                            if (emotionResponse != null) {
                                val replyText = "${emotionResponse.message}\nЭмоция: ${emotionResponse.emotion} (уверенность: ${String.format("%.2f", emotionResponse.confidence)})"
                                val receivedMessage = Message(replyText, isSent = false)
                                messageAdapter.addMessage(receivedMessage)
                            } else {
                                val errorMessage = Message("Ошибка: Нет данных от сервера", isSent = false)
                                messageAdapter.addMessage(errorMessage)
                            }
                        } else {
                            val errorMessage = Message("Ошибка сервера. Попробуйте перезагрузить приложение", isSent = false)
                            messageAdapter.addMessage(errorMessage)
                        }
                        // Scroll to the latest message
                        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                    }

                    override fun onFailure(call: Call<EmotionResponse>, t: Throwable) {
                        android.util.Log.d("AiChatFragment", "Error: ${t.message}")
                        val customMessage = Message(
                            "Привет. Поздравляем! Ты заставил нашего бота хорошенько призадуматься. Пока что он не может дать ответ на твой вопрос",
                            isSent = false
                        )
                        messageAdapter.addMessage(customMessage)
                        // Scroll to the latest message
                        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                    }
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}