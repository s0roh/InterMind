package com.soroh.intermind.feature.auth.impl.di

import com.soroh.intermind.core.navigation.deeplink.FeatureDeepLinkParser
import com.soroh.intermind.feature.auth.impl.util.AuthDeepLinkParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal interface AuthDeepLinkModule {

    @Binds
    @IntoSet
    fun bindAuthDeepLinkParser(parser: AuthDeepLinkParser): FeatureDeepLinkParser
}