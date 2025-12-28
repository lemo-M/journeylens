package com.lm.journeylens.feature.memory.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * 草稿确认对话框类型
 */
enum class DraftDialogType {
    /** 进入 AddTab 时发现草稿 */
    RESUME_OR_RESTART,
    /** 从地图"去添加"时发现草稿 */
    CONTINUE_OR_NEW,
    /** 从照片选择页返回时询问是否保存草稿 */
    EXIT_SAVE_OR_DISCARD
}

/**
 * 草稿确认对话框
 */
@Composable
fun DraftConfirmDialog(
    dialogType: DraftDialogType,
    photoCount: Int,
    onConfirm: () -> Unit,  // 继续/使用草稿
    onDismiss: () -> Unit   // 重新开始/新建
) {
    Dialog(onDismissRequest = { /* 不允许点击外部关闭 */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = JourneyLensColors.SurfaceLight
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题
                Text(
                    text = when (dialogType) {
                        DraftDialogType.EXIT_SAVE_OR_DISCARD -> "保存草稿？"
                        else -> "发现未完成的草稿"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = JourneyLensColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 描述
                Text(
                    text = when (dialogType) {
                        DraftDialogType.RESUME_OR_RESTART -> 
                            "您有一份未完成的记忆草稿" + 
                            (if (photoCount > 0) "，包含 $photoCount 张照片" else "")
                        DraftDialogType.CONTINUE_OR_NEW -> 
                            "您有一份未完成的草稿，要继续编辑还是在此处新建？"
                        DraftDialogType.EXIT_SAVE_OR_DISCARD ->
                            "您已选择了 $photoCount 张照片，是否保存为草稿以便下次继续？"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneyLensColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 次要按钮 (重新开始/新建)
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = JourneyLensColors.TextSecondary
                        )
                    ) {
                        Text(
                            when (dialogType) {
                                DraftDialogType.RESUME_OR_RESTART -> "重新开始"
                                DraftDialogType.CONTINUE_OR_NEW -> "在此新建"
                                DraftDialogType.EXIT_SAVE_OR_DISCARD -> "不保存"
                            }
                        )
                    }
                    
                    // 主要按钮 (继续编辑/使用草稿)
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JourneyLensColors.AppleBlue
                        )
                    ) {
                        Text(
                            when (dialogType) {
                                DraftDialogType.RESUME_OR_RESTART -> "继续编辑"
                                DraftDialogType.CONTINUE_OR_NEW -> "继续草稿"
                                DraftDialogType.EXIT_SAVE_OR_DISCARD -> "保存草稿"
                            }
                        )
                    }
                }
            }
        }
    }
}
