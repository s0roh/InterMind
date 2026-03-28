package com.soroh.intermind.feature.training.impl

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.soroh.intermind.core.designsystem.component.AppButton
import com.soroh.intermind.core.designsystem.component.ErrorState
import com.soroh.intermind.core.designsystem.component.LoadingState
import com.soroh.intermind.core.domain.entity.TestType
import com.soroh.intermind.feature.training.api.R
import com.soroh.intermind.feature.training.impl.component.AnswerWithHighlight
import com.soroh.intermind.feature.training.impl.component.ChoiceButton
import com.soroh.intermind.feature.training.impl.component.EmptyTrainingState
import com.soroh.intermind.feature.training.impl.component.TFButton
import com.soroh.intermind.feature.training.impl.component.TrueFalseAnswerSection
import com.soroh.intermind.feature.training.impl.component.UserInputWithHighlight

@Composable
fun TrainingScreen(
    viewModel: TrainingViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    when (val currentState = state) {
        is TrainingScreenState.Initial,
        is TrainingScreenState.Loading -> LoadingState()

        is TrainingScreenState.Empty -> EmptyTrainingState(onBackClick = onBackClick)

        is TrainingScreenState.Error -> ErrorState(message = currentState.message)

        is TrainingScreenState.Finished -> {
            TrainingFinishedScreen(
                state = currentState,
                onFinish = onBackClick
            )
        }

        is TrainingScreenState.InProgress -> {
            TrainingContent(
                state = currentState,
                onSubmitAnswer = viewModel::submitAnswer,
                onNext = viewModel::next,
                onExitClick = { showExitDialog = true }
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
private fun TrainingContent(
    state: TrainingScreenState.InProgress,
    onSubmitAnswer: (String?) -> Unit,
    onNext: () -> Unit,
    onExitClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Тренировка") },
                navigationIcon = {
                    IconButton(onClick = onExitClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_training_api_exit)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TrainingCardContent(
                state = state,
                onSubmitAnswer = onSubmitAnswer,
                modifier = Modifier.weight(1f)
            )

            BottomBar(
                isRevealed = state.isAnswerRevealed,
                onNext = onNext,
                onDontKnow = { onSubmitAnswer(null) }
            )

        }
    }
}

@Composable
private fun TrainingCardContent(
    state: TrainingScreenState.InProgress,
    onSubmitAnswer: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        state.preloadedPicture?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
        }

        Text(
            text = state.currentCard.question,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(20.dp))

        when (state.currentCard.testType) {
            TestType.CHOICE -> ChoiceCard(state, onSubmitAnswer)
            TestType.TRUE_FALSE -> TrueFalseCard(state, onSubmitAnswer)
            TestType.INPUT -> InputCard(state, onSubmitAnswer)
        }
    }
}

@Composable
private fun ChoiceCard(
    state: TrainingScreenState.InProgress,
    onSubmit: (String) -> Unit
) {
    val answers = rememberSaveable(state.currentCard.id) {
        (listOf(state.currentCard.answer) + state.currentCard.wrongAnswers).shuffled()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        answers.forEach { answer ->
            ChoiceButton(
                answer = answer,
                isAnswered = state.isAnswerRevealed,
                selectedAnswer = state.selectedAnswer,
                correctAnswer = state.currentCard.answer,
                onAnswerSelected = { onSubmit(answer) }
            )
        }
    }
}

@Composable
private fun TrueFalseCard(
    state: TrainingScreenState.InProgress,
    onSubmit: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrueFalseAnswerSection(
            displayedAnswer = state.currentCard.displayedAnswer,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TFButton(
                text = stringResource(R.string.feature_training_api_false_answer),
                isButtonTrue = false,
                state = state,
                onClick = { onSubmit(null) },
                modifier = Modifier.weight(1f)
            )

            TFButton(
                text = stringResource(R.string.feature_training_api_true_answer),
                isButtonTrue = true,
                state = state,
                onClick = { onSubmit(state.currentCard.answer) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InputCard(
    state: TrainingScreenState.InProgress,
    onSubmit: (String) -> Unit
) {
    var text by remember(state.currentCard) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (state.isAnswerRevealed) {
            AnswerWithHighlight(
                answer = state.currentCard.answer,
                missingWords = state.currentCard.missingWords,
                startIndex = state.currentCard.missingWordStartIndex
            )

            UserInputWithHighlight(
                userInput = state.selectedAnswer ?: "",
                missingWords = state.currentCard.missingWords,
                isCorrect = state.isCorrect ?: false
            )

            Spacer(modifier = Modifier.height(24.dp))

        } else {
            Text(
                text = if (state.currentCard.partialAnswer.isNullOrEmpty())
                    stringResource(R.string.feature_training_api_give_answer)
                else stringResource(R.string.feature_training_api_complete_answer),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            state.currentCard.partialAnswer?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.feature_training_api_enter_answer)) },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (text.isNotBlank()) onSubmit(text) }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onSubmit(text) },
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.feature_training_api_submit_button))
            }
        }
    }
}

@Composable
private fun BottomBar(
    isRevealed: Boolean,
    onNext: () -> Unit,
    onDontKnow: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {

        if (!isRevealed) {
            TextButton(
                onClick = onDontKnow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.feature_training_api_dont_know))
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.feature_training_api_next))
            }
        }
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
                StatRow(stringResource(R.string.feature_training_api_total_cards, state.totalCards))
                StatRow(
                    stringResource(
                        R.string.feature_training_api_correct_answers,
                        state.correctCount
                    )
                )
                StatRow(stringResource(R.string.feature_training_api_accuracy, accuracy))
                StatRow(stringResource(R.string.feature_training_api_time_spent, timeFormatted))
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