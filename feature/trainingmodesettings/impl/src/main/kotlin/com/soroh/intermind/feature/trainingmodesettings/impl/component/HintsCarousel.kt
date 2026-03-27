package com.soroh.intermind.feature.trainingmodesettings.impl.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.soroh.intermind.feature.trainingmodesettings.impl.util.HintHelpItem

@Composable
internal fun HintsCarousel(hints: List<HintHelpItem>) {
    val pagerState = rememberPagerState(pageCount = { hints.size })
    val maxHintHeightDp = remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    Column {
        HorizontalPager(state = pagerState) { page ->
            val hint = hints[page]

            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        if (maxHintHeightDp.value == null) {
                            maxHintHeightDp.value = with(density) {
                                coordinates.size.height.toDp()
                            }
                        }
                    }
                    .height(maxHintHeightDp.value ?: Dp.Unspecified)
                    .fillMaxWidth()
            ) {
                HintCard(
                    imageResId = hint.imageResId,
                    titleResId = hint.titleResId,
                    descriptionResId = hint.descriptionResId
                )
            }
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                    pagerState.animateScrollToPage(currentPage)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PagerIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}