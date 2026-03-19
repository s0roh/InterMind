package com.soroh.intermind.feature.deckdetails.impl.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.feature.deckdetails.api.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCardItem(
    card: Card,
    deckId: String,
    isOwner: Boolean,
    expandedCardId: MutableState<String?>,
    isMenuExpanded: MutableState<Boolean>,
    sheetState: SheetState,
    coroutineScope: CoroutineScope,
    onEditCard: (deckId: String, cardId: String) -> Unit,
    onDeleteCard: (Card) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = expandedCardId.value == card.id

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    expandedCardId.value =
                        if (expandedCardId.value == card.id) null else card.id
                })
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = card.question,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (isOwner) {
                    Box {
                        IconButton(onClick = { isMenuExpanded.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.feature_deckdetails_api_menu)
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded.value,
                            onDismissRequest = { isMenuExpanded.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.feature_deckdetails_api_edit)) },
                                onClick = {
                                    isMenuExpanded.value = false
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onEditCard(deckId, card.id)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.feature_deckdetails_api_delete)) },
                                onClick = { onDeleteCard(card) }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    when {
                        card.attachment.isNullOrBlank() -> {
//                            Box(
//                                modifier = Modifier
//                                    .width(144.dp)
//                                    .height(97.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                CircularProgressIndicator()
//                            }
                        }

                        else -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.attachment)
                                    .memoryCachePolicy(CachePolicy.DISABLED)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .build(),
                                contentDescription = stringResource(R.string.feature_deckdetails_api_image_of_card),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(81.dp)
                                    .width(144.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Text(
                        text = stringResource(R.string.feature_deckdetails_api_answer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}