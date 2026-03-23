package com.soroh.intermind.feature.training.impl

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.soroh.intermind.core.designsystem.component.AppButton
import com.soroh.intermind.core.designsystem.component.AppElevatedButton
import com.soroh.intermind.core.designsystem.component.ErrorState
import com.soroh.intermind.core.designsystem.component.LoadingState
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.core.domain.entity.TrainingCard
import com.soroh.intermind.feature.training.api.R

@Composable
fun TrainingScreen(
    viewModel: TrainingViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    when (val currentState = state) {
        is TrainingScreenState.Initial,
        is TrainingScreenState.Loading -> {
            LoadingState()
        }

        is TrainingScreenState.Empty -> {
            EmptyTrainingState(onBackClick = onBackClick)
        }

        is TrainingScreenState.InProgress -> {
            TrainingInProgressScreen(
                state = currentState,
                onSelectAnswer = viewModel::selectAnswer,
                onSubmitTextAnswer = viewModel::submitTextAnswer,
                onRevealAnswer = viewModel::revealAnswer,
                onNextCard = viewModel::nextCard,
                onExitClick = { showExitDialog = true }
            )
        }

        is TrainingScreenState.Finished -> {
            TrainingFinishedScreen(
                state = currentState,
                onFinish = onBackClick
            )
        }

        is TrainingScreenState.Error -> {
            ErrorState(
                message = currentState.message
            )
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
                viewModel.finishTraining()
            },
            onDismiss = { showExitDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingInProgressScreen(
    state: TrainingScreenState.InProgress,
    onSelectAnswer: (String) -> Unit,
    onSubmitTextAnswer: (String) -> Unit,
    onRevealAnswer: () -> Unit,
    onNextCard: () -> Unit,
    onExitClick: () -> Unit
) {
    val progress = state.cardNumber.toFloat() / state.totalCards.toFloat()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.feature_training_api_card_progress,
                            state.cardNumber,
                            state.totalCards
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExitClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_training_api_exit)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Card content - scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                CardContent(
                    card = state.currentCard,
                    preloadedPicture = state.preloadedPicture,
                    isAnswerRevealed = state.isAnswerRevealed,
                    selectedAnswer = state.selectedAnswer,
                    isCorrect = state.isCorrect,
                    onSelectAnswer = onSelectAnswer,
                    onSubmitTextAnswer = onSubmitTextAnswer,
                    onRevealAnswer = onRevealAnswer
                )
            }

            // Bottom section - buttons fixed at bottom
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AnimatedContent(
                    targetState = state.isAnswerRevealed,
                    label = "bottomButtons"
                ) { revealed ->
                    if (!revealed) {
                        // Show answer button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppElevatedButton(
                                title = stringResource(R.string.feature_training_api_show_answer),
                                onClick = onRevealAnswer,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Next button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppButton(
                                title = stringResource(R.string.feature_training_api_next),
                                onClick = onNextCard,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardContent(
    card: TrainingCard,
    preloadedPicture: Any?,
    isAnswerRevealed: Boolean,
    selectedAnswer: String?,
    isCorrect: Boolean?,
    onSelectAnswer: (String) -> Unit,
    onSubmitTextAnswer: (String) -> Unit,
    onRevealAnswer: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCorrect == null -> MaterialTheme.colorScheme.surface
            isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        },
        label = "cardBackground"
    )

    Column {
        // Card with question and answer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image if available
                preloadedPicture?.let { picture ->
                    Image(
                        painter = rememberAsyncImagePainter(picture),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Question
                Text(
                    text = card.question,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                // Show answer if revealed
                if (isAnswerRevealed) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Result indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCorrect == true) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCorrect == true) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isCorrect == true)
                                stringResource(R.string.feature_training_api_correct)
                            else
                                stringResource(R.string.feature_training_api_incorrect),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect == true)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }

                    // Show correct answer if wrong
                    if (isCorrect != true) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = card.answer,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Answer options (only show if not revealed)
        if (!isAnswerRevealed) {
            when (card.testType) {
                TestType.CHOICE -> {
                    ChoiceAnswerSection(
                        correctAnswer = card.answer,
                        wrongAnswers = card.wrongAnswers,
                        onSelectAnswer = onSelectAnswer
                    )
                }

                TestType.TRUE_FALSE -> {
                    TrueFalseAnswerSection(
                        correctAnswer = card.answer,
                        displayedAnswer = card.displayedAnswer,
                        onSelectAnswer = onSelectAnswer
                    )
                }

                TestType.INPUT -> {
                    InputAnswerSection(
                        partialAnswer = card.partialAnswer,
                        onSubmit = onSubmitTextAnswer
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceAnswerSection(
    correctAnswer: String,
    wrongAnswers: List<String>,
    onSelectAnswer: (String) -> Unit
) {
    val allAnswers = (wrongAnswers + correctAnswer).shuffled()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        allAnswers.forEach { answer ->
            OutlinedButton(
                onClick = { onSelectAnswer(answer) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun TrueFalseAnswerSection(
    correctAnswer: String,
    displayedAnswer: String?,
    onSelectAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the answer user needs to evaluate
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = displayedAnswer ?: correctAnswer,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onSelectAnswer(displayedAnswer ?: "") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = stringResource(R.string.feature_training_api_no),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            AppButton(
                title = stringResource(R.string.feature_training_api_yes),
                onClick = { onSelectAnswer(correctAnswer) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InputAnswerSection(
    partialAnswer: String?,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column {
        // Show partial answer hint
        partialAnswer?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$it...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.feature_training_api_enter_answer)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onSubmit(text) })
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppButton(
            title = stringResource(R.string.feature_training_api_submit),
            onClick = { onSubmit(text) },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TrainingFinishedScreen(
    state: TrainingScreenState.Finished,
    onFinish: () -> Unit
) {
    val accuracy = if (state.totalCards > 0) {
        (state.correctCount * 100) / state.totalCards
    } else 0

    val minutes = state.durationSec / 60
    val seconds = state.durationSec % 60
    val timeFormatted = if (minutes > 0) {
        "${minutes}м ${seconds}с"
    } else {
        "${seconds}с"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.feature_training_api_training_completed),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow(
                    label = stringResource(R.string.feature_training_api_total_cards, state.totalCards)
                )
                StatRow(
                    label = stringResource(R.string.feature_training_api_correct_answers, state.correctCount)
                )
                StatRow(
                    label = stringResource(R.string.feature_training_api_accuracy, accuracy)
                )
                StatRow(
                    label = stringResource(R.string.feature_training_api_time_spent, timeFormatted)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AppButton(
            title = stringResource(R.string.feature_training_api_finish),
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun EmptyTrainingState(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.feature_training_api_no_cards_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.feature_training_api_no_cards_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            title = stringResource(R.string.feature_training_api_go_back),
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_training_api_exit_title)) },
        text = { Text(stringResource(R.string.feature_training_api_exit_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.feature_training_api_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_training_api_no))
            }
        }
    )
}