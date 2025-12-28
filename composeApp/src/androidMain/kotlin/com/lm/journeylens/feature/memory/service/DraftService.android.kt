package com.lm.journeylens.feature.memory.service

import android.content.Context
import com.lm.journeylens.feature.memory.AddMemoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File

/**
 * Android 草稿服务实现
 */
actual class DraftService(private val context: Context) {
    private val draftFile by lazy { File(context.filesDir, "add_memory_draft.json") }
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    actual suspend fun saveDraft(state: AddMemoryUiState) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(serializer<AddMemoryUiState>(), state)
                draftFile.writeText(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    actual suspend fun loadDraft(): AddMemoryUiState? {
        return withContext(Dispatchers.IO) {
            if (draftFile.exists()) {
                try {
                    json.decodeFromString(serializer<AddMemoryUiState>(), draftFile.readText())
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

