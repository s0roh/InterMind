package com.soroh.intermind.fearure.addeditcard.impl

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soroh.intermind.core.designsystem.component.AppButton
import com.soroh.intermind.core.designsystem.component.CenteredTopAppBar
import com.soroh.intermind.core.designsystem.component.LoadingState
import com.soroh.intermind.core.designsystem.component.NavigationIconType
import com.soroh.intermind.core.designsystem.component.TextFieldWithError
import com.soroh.intermind.core.designsystem.icon.InterMindIcons
import com.soroh.intermind.fearure.addeditcard.api.R
import com.soroh.intermind.fearure.addeditcard.impl.component.CardPicture
import com.soroh.intermind.fearure.addeditcard.impl.component.WrongAnswersSection
import com.soroh.intermind.fearure.addeditcard.impl.util.CropImageContract
import com.soroh.intermind.fearure.addeditcard.impl.util.launchCrop

@Composable
fun AddEditCardScreen(
    viewModel: AddEditCardViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val themeColors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddEditCardEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                AddEditCardEvent.NavigateBack -> {
                    onSaveClick()
                }
            }
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            viewModel.setCardPicture(result.uriContent.toString())
        }
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { launchCrop(uri, cropImageLauncher::launch, themeColors) }
        }

    Scaffold(
        topBar = {
            CenteredTopAppBar(
                title = if (viewModel.key.cardId == null)
                    stringResource(R.string.feature_addeditcard_api_card_creation)
                else
                    stringResource(R.string.feature_addeditcard_api_edit_card),
                navigationIconType = NavigationIconType.BACK,
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState()
        } else {
            AddEditCardForm(
                modifier = Modifier
                    .padding(paddingValues)
                    .imePadding(),
                cardId = viewModel.key.cardId,
                screenState = uiState,
                onQuestionChange = viewModel::onQuestionChanged,
                onAnswerChange = viewModel::onAnswerChanged,
                onSave = viewModel::saveCard,
                onAddPictureClick = { pickImageLauncher.launch("image/*") },
                focusManager = focusManager,
                viewModel = viewModel
            )
        }
    }
}

@Composable
internal fun AddEditCardForm(
    modifier: Modifier = Modifier,
    cardId: String?,
    screenState: AddEditCardUiState,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
    onSave: () -> Unit,
    onAddPictureClick: () -> Unit,
    focusManager: FocusManager,
    viewModel: AddEditCardViewModel,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 45.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextFieldWithError(
                value = screenState.question,
                onValueChange = onQuestionChange,
                labelResId = R.string.feature_addeditcard_api_question,
                error = screenState.questionError,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedContent(
                targetState = screenState.cardPictureUri,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "CardPictureSwitcher"
            ) { targetImageUri ->
                if (targetImageUri == null) {
                    TextButton(
                        onClick = onAddPictureClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(InterMindIcons.Image),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.feature_addeditcard_api_add_image))
                    }
                } else {
                    CardPicture(
                        imageUri = targetImageUri,
                        onClick = onAddPictureClick,
                        onSwipeToDelete = { viewModel.deleteCardPicture() }
                    )
                }
            }

            TextFieldWithError(
                value = screenState.answer,
                onValueChange = onAnswerChange,
                labelResId = R.string.feature_addeditcard_api_answer,
                error = screenState.answerError,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    onSave()
                },
                modifier = Modifier.fillMaxWidth()
            )

            WrongAnswersSection(
                wrongAnswers = screenState.wrongAnswerList,
                onAddWrongAnswer = { viewModel.addWrongAnswerField() },
                onWrongAnswerChanged = { index, value ->
                    viewModel.updateWrongAnswer(
                        index,
                        value
                    )
                },
                onRemoveWrongAnswer = { index -> viewModel.removeWrongAnswer(index) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        AppButton(
            title = if (cardId == null) stringResource(R.string.feature_addeditcard_api_add_card)
            else stringResource(R.string.feature_addeditcard_api_edit_card),
            onClick = onSave,
            enabled = screenState.isSaveButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

