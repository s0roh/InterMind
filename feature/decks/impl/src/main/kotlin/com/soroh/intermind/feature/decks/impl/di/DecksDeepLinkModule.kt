package com.soroh.intermind.feature.decks.impl.di

import com.soroh.intermind.core.navigation.deeplink.FeatureDeepLinkParser
import com.soroh.intermind.feature.decks.impl.util.DecksDeepLinkParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal interface DecksDeepLinkModule {

    @Binds
    @IntoSet
    fun bindDecksDeepLinkParser(parser: DecksDeepLinkParser): FeatureDeepLinkParser
}