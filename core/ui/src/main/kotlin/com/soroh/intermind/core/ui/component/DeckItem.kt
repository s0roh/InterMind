package com.soroh.intermind.core.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soroh.intermind.core.designsystem.component.ThemePreviews
import com.soroh.intermind.core.designsystem.icon.InterMindIcons
import com.soroh.intermind.core.designsystem.theme.InterMindTheme
import com.soroh.intermind.core.ui.R
import com.soroh.intermind.core.ui.model.DeckUiModel
import com.soroh.intermind.core.ui.util.formatCount

enum class DeckDisplayMode {
    SOCIAL,
    PERSONAL,
}

@Composable
fun DeckItem(
    modifier: Modifier = Modifier,
    deck: DeckUiModel,
    mode: DeckDisplayMode,
    onDeckClick: (String) -> Unit = {},
    onLikeClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, MaterialTheme.shapes.medium)
            .clickable(onClick = { onDeckClick(deck.id) }),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        icon = ImageVector.vectorResource(InterMindIcons.Cards),
                        value = deck.cardsCount.toString()
                    )

                    if (mode == DeckDisplayMode.SOCIAL) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatItem(
                                icon = ImageVector.vectorResource(InterMindIcons.Trainings),
                                value = formatCount(deck.trainings)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            LikeButtonWithAnimation(
                                likeCount = deck.likes,
                                isLiked = deck.isLiked,
                                onClick = onLikeClick
                            )
                        }
                    } else {
                        DeckVisibility(deck.isPublic)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckVisibility(isPublic: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(
                if (isPublic) InterMindIcons.PublicStatus
                else InterMindIcons.PrivateStatus
            ),
            modifier = Modifier.size(16.dp),
            tint = Color.Gray,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isPublic) stringResource(R.string.public_deck) else stringResource(R.string.private_deck),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun LikeButtonWithAnimation(
    likeCount: Int,
    isLiked: Boolean,
    onClick: () -> Unit,
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isLiked) 1.01f else 1f,
        animationSpec = keyframes {
            durationMillis = 600
            1f at 0 using FastOutSlowInEasing
            1.3f at 150 using FastOutSlowInEasing
            1f at 300 using FastOutSlowInEasing
            1.1f at 450 using FastOutSlowInEasing
            1f at 600 using FastOutSlowInEasing
        }
    )

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        AnimatedLikeCount(likeCount, isLiked)
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            tint = if (isLiked) MaterialTheme.colorScheme.error else Color.Gray,
            contentDescription = null,
            modifier = Modifier.scale(animatedScale)
        )
    }
}

@Composable
private fun AnimatedLikeCount(
    likeCount: Int,
    isLiked: Boolean,
) {
    AnimatedContent(
        targetState = likeCount,
        label = "LikeCountTransition"
    ) { count ->
        if (count != 0) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = formatCount(count),
                style = MaterialTheme.typography.bodyMedium.copy(color = if (isLiked) MaterialTheme.colorScheme.error else Color.Gray)
            )
        }
    }
}

@ThemePreviews
@Composable
private fun DeckItemPreview() {
    InterMindTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                DeckItem(
                    deck = DeckUiModel(
                        id = "1",
                        name = "Английский для начинающих",
                        isPublic = true,
                        isLiked = false,
                        cardsCount = 250,
                        likes = 15420,
                        trainings = 892
                    ),
                    mode = DeckDisplayMode.PERSONAL
                )

                Spacer(modifier = Modifier.height(8.dp))

                DeckItem(
                    deck = DeckUiModel(
                        id = "2",
                        name = "Фразовые глаголы",
                        isPublic = false,
                        isLiked = false,
                        cardsCount = 120,
                        likes = 0,
                        trainings = 342
                    ),
                    mode = DeckDisplayMode.PERSONAL
                )

                Spacer(modifier = Modifier.height(16.dp))

                DeckItem(
                    deck = DeckUiModel(
                        id = "3",
                        name = "Английский для начинающих",
                        isPublic = true,
                        isLiked = true,
                        cardsCount = 500,
                        likes = 8923,
                        trainings = 2341
                    ),
                    mode = DeckDisplayMode.SOCIAL
                )

                Spacer(modifier = Modifier.height(8.dp))

                DeckItem(
                    deck = DeckUiModel(
                        id = "4",
                        name = "Фразовые глаголы",
                        isPublic = true,
                        isLiked = false,
                        cardsCount = 1200,
                        likes = 15420,
                        trainings = 5678
                    ),
                    mode = DeckDisplayMode.SOCIAL
                )
            }
        }
    }
}