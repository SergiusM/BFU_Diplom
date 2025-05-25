package com.example.test_for_diplom

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage


object Supabase {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://liertfbnjromcbskbpbu.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpZXJ0ZmJuanJvbWNic2ticGJ1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4MjkzMzAsImV4cCI6MjA2MzQwNTMzMH0.JDnqIX33lOsPE2-PKM3RQz1hXCs9xrrqd2ycFilWrN0"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}