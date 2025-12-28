package com.lm.journeylens.feature.memory.service

import android.content.Context
import com.lm.journeylens.feature.memory.AddMemoryUiState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android 草稿服务实现
 */
actual class DraftService(private val context: Context) {
    private val draftFile by lazy { File(context.filesDir, "add_memory_draft.json") }
    
    actual suspend fun saveDraft(state: AddMemoryUiState) {
        withContext(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(state)
                draftFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    actual suspend fun loadDraft(): AddMemoryUiState? {
        return withContext(Dispatchers.IO) {
            if (draftFile.exists()) {
                try {
                    Json.decodeFromString<AddMemoryUiState>(draftFile.readText())
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }
    }
    
    actual suspend fun clearDraft() {
         withContext(Dispatchers.IO) {
             if (draftFile.exists()) {
                 draftFile.delete()
             }
         }
    }
}
