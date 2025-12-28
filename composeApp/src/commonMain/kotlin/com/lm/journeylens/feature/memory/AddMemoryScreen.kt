package com.lm.journeylens.feature.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.component.DraftConfirmDialog
import com.lm.journeylens.feature.memory.component.DraftDialogType
import com.lm.journeylens.feature.memory.presentation.steps.DetailsStep
import com.lm.journeylens.feature.memory.presentation.steps.LocationStep
import com.lm.journeylens.feature.memory.presentation.steps.PhotosStep
import com.lm.journeylens.feature.memory.presentation.steps.SuccessStep

/**
 * 添加记忆页面
 * 新流程：选位置 → 选照片 → 填写详情
 * 草稿检测：进入"选照片"步骤前检测是否有照片草稿
 */
@Composable
fun AddMemoryScreen(screenModel: AddMemoryScreenModel) {
    val uiState by screenModel.uiState.collectAsState()
    val showDraftDialog by screenModel.showDraftDialog.collectAsState()
    val showExitConfirmDialog by screenModel.showExitConfirmDialog.collectAsState()
    val draftPhotoCount by screenModel.draftPhotoCount.collectAsState()
    
    val currentStep = uiState.step
    
    // 草稿恢复对话框
    if (showDraftDialog) {
        DraftConfirmDialog(
            dialogType = DraftDialogType.RESUME_OR_RESTART,
            photoCount = draftPhotoCount,
            onConfirm = { screenModel.restoreDraftPhotos() },
            onDismiss = { screenModel.discardDraft() }
        )
    }
    
    // 退出确认对话框（从照片选择页返回时）
    if (showExitConfirmDialog) {
        DraftConfirmDialog(
            dialogType = DraftDialogType.EXIT_SAVE_OR_DISCARD,
            photoCount = draftPhotoCount,
            onConfirm = { screenModel.confirmExitWithSave() },
            onDismiss = { screenModel.confirmExitWithoutSave() }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background)
    ) {
        when (currentStep) {
            ImportStep.LOCATION -> LocationStep(
                onUseCurrentLocation = { lat, lng, name ->
                    screenModel.setLocationFromGps(lat, lng, name)
                },
                onSelectFromMap = { lat, lng ->
                    screenModel.setLocationFromMap(lat, lng)
                }
            )
            ImportStep.PHOTOS -> PhotosStep(
                photoUris = uiState.photoUris,
                onAddPhotos = { screenModel.addPhotos(it) },
                onRemovePhoto = { screenModel.removePhoto(it) },
                onConfirm = { screenModel.confirmPhotos() },
                onBack = { screenModel.requestExitFromPhotos() }
            )
            ImportStep.DETAILS -> DetailsStep(
                photoUris = uiState.photoUris,
                emoji = uiState.emoji,
                note = uiState.note,
                onEmojiChange = { screenModel.updateEmoji(it) },
                onNoteChange = { screenModel.updateNote(it) },
                onSave = { screenModel.saveMemory() },
                onBack = { screenModel.goBack() }
            )
            ImportStep.SUCCESS -> SuccessStep(
                onDone = { screenModel.reset() }
            )
        }
        
        // Loading 遮罩
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JourneyLensColors.Background.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = JourneyLensColors.AppleBlue)
            }
        }
    }
}
