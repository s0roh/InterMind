package com.soroh.intermind.feature.deckdetails.impl.util

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.net.toUri

val imageUriCacheSaver = Saver<SnapshotStateMap<Int, Uri?>, Map<Int, String?>>(
    save = { map ->
        map.mapValues { entry -> entry.value?.toString() }
    },
    restore = { map ->
        mutableStateMapOf(
            *map.mapValues { entry -> entry.value?.toUri() }.toList().toTypedArray()
        )
    }
)

@Composable
fun <K, V> rememberSavableWithMap(
    initialValue: () -> SnapshotStateMap<K, V> ,
    saver: Saver<SnapshotStateMap<K, V>, Map<K, String?>>
): SnapshotStateMap<K, V> {
    return rememberSaveable(saver = saver) { initialValue() }
}