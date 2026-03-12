package com.soroh.intermind.feature.auth.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://kmzvykougtykprzotyrr.supabase.co",
    supabaseKey = "sb_publishable_xHIxDmCM8N9kgAsnzC7JrQ_VtbhJ_LJ"
) {
    install(Auth)
}
