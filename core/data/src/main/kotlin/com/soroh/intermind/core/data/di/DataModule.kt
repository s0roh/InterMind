package com.soroh.intermind.core.data.di

import com.soroh.intermind.core.data.repository.AuthRepository
import com.soroh.intermind.core.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun provideSupabaseClient(): SupabaseClient {
            return createSupabaseClient(
                supabaseUrl = "https://kmzvykougtykprzotyrr.supabase.co",
                supabaseKey = "sb_publishable_xHIxDmCM8N9kgAsnzC7JrQ_VtbhJ_LJ"
            ) {
                install(Auth) {
                    host = "intermind.com"
                    scheme = "app"
                }
            }
        }
    }
}