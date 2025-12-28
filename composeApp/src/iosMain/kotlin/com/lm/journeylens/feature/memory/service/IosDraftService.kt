package com.lm.journeylens.feature.memory.service

import com.lm.journeylens.feature.memory.AddMemoryUiState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * iOS 草稿服务实现
 */
class IosDraftService : DraftService {
    private val fileManager = NSFileManager.defaultManager
    private val documentsUrl = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
    private val fileUrl = documentsUrl.URLByAppendingPathComponent("add_memory_draft.json")

    override suspend fun saveDraft(state: AddMemoryUiState) {
        withContext(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(state)
                val data = NSString.create(string = json).dataUsingEncoding(NSUTF8StringEncoding)
                data?.writeToURL(fileUrl!!, true)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override suspend fun loadDraft(): AddMemoryUiState? {
        return withContext(Dispatchers.IO) {
            try {
                if (fileManager.fileExistsAtPath(fileUrl!!.path!!)) {
                    val data = NSData.dataWithContentsOfURL(fileUrl!!)
                    val json = NSString.create(data = data!!, encoding = NSUTF8StringEncoding).toString()
                    Json.decodeFromString<AddMemoryUiState>(json)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun clearDraft() {
        withContext(Dispatchers.IO) {
             try {
                if (fileManager.fileExistsAtPath(fileUrl!!.path!!)) {
                    fileManager.removeItemAtURL(fileUrl!!, null)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
