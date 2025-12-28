package com.lm.journeylens.feature.memory.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * 成功页面
 */
@Composable
fun SuccessStep(
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "保存成功！",
            style = MaterialTheme.typography.headlineLarge,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "记忆已添加到你的时间轴",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("继续添加")
        }
    }
}
