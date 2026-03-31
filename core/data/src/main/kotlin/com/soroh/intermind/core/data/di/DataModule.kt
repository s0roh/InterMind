package com.soroh.intermind.core.data.di

import com.soroh.intermind.core.data.BuildConfig
import com.soroh.intermind.core.data.repository.AuthRepository
import com.soroh.intermind.core.data.repository.AuthRepositoryImpl
import com.soroh.intermind.core.data.repository.DecksRepository
import com.soroh.intermind.core.data.repository.DecksRepositoryImpl
import com.soroh.intermind.core.data.repository.ExploreRepository
import com.soroh.intermind.core.data.repository.ExploreRepositoryImpl
import com.soroh.intermind.core.data.repository.HistoryRepository
import com.soroh.intermind.core.data.repository.HistoryRepositoryImpl
import com.soroh.intermind.core.data.repository.ProfileRepository
import com.soroh.intermind.core.data.repository.ProfileRepositoryImpl
import com.soroh.intermind.core.data.repository.TrainingRepository
import com.soroh.intermind.core.data.repository.TrainingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    fun bindDecksRepository(decksRepositoryImpl: DecksRepositoryImpl): DecksRepository

    @Binds
    @Singleton
    fun bindTrainingRepository(trainingRepositoryImpl: TrainingRepositoryImpl): TrainingRepository

    @Binds
    @Singleton
    fun bindExploreRepository(exploreRepositoryImpl: ExploreRepositoryImpl): ExploreRepository

    @Binds
    @Singleton
    fun bindHistoryRepository(historyRepositoryImpl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

    companion object {

        @Provides
        @Singleton
        fun provideSupabaseClient(): SupabaseClient {
            return createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_KEY
            ) {
                install(Auth) {
                    host = "intermind.com"
                    scheme = "app"
                }
                install(Postgrest)
                install(Storage)
            }
        }
    }
}