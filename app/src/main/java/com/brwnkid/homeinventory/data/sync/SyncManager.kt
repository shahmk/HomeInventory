package com.brwnkid.homeinventory.data.sync

import android.content.Context
import com.brwnkid.homeinventory.data.InventoryRepository
import com.brwnkid.homeinventory.data.Item
import com.brwnkid.homeinventory.data.Location
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class SyncManager(
    private val context: Context,
    private val repository: InventoryRepository,
    private val authManager: GoogleDriveAuthManager
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private fun getDriveService(): Drive? {
        val credential = authManager.getCredential() ?: return null
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Home Inventory").build()
    }

    suspend fun sync(): Result<Unit> = withContext(Dispatchers.IO) {
        val driveService = getDriveService() ?: return@withContext Result.failure(Exception("Not signed in"))

        try {
            // 1. Ensure root folder exists
            val rootId = getOrCreateFolder(driveService, "root", "Home Inventory App")
            
            // 2. Ensure subfolders exist
            val locationsDirId = getOrCreateFolder(driveService, rootId, "locations")
            val itemsDirId = getOrCreateFolder(driveService, rootId, "items")
            val imagesDirId = getOrCreateFolder(driveService, rootId, "images")

            // 3. Pull from Cloud
            pullLocations(driveService, locationsDirId)
            pullItems(driveService, itemsDirId)

            // 4. Push to Cloud
            pushLocations(driveService, locationsDirId)
            pushItems(driveService, itemsDirId)
            
            // 5. Sync Images
            pushImages(driveService, imagesDirId)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun getOrCreateFolder(driveService: Drive, parentId: String, name: String): String {
        val query = "mimeType = 'application/vnd.google-apps.folder' and '$parentId' in parents and name = '$name' and trashed = false"
        val result = driveService.files().list().setQ(query).setSpaces("drive").execute()
        val files = result.files
        if (!files.isNullOrEmpty()) {
            return files[0].id
        }
        
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            this.name = name
            this.mimeType = "application/vnd.google-apps.folder"
            this.parents = listOf(parentId)
        }
        val file = driveService.files().create(fileMetadata).setFields("id").execute()
        return file.id
    }

    private suspend fun pullLocations(driveService: Drive, folderId: String) {
        val query = "'$folderId' in parents and trashed = false"
        val files = driveService.files().list().setQ(query).setFields("files(id, name, modifiedTime)").execute().files ?: return
        
        files.forEach { file ->
            if (file.name.endsWith(".json")) {
                try {
                    val content = readFile(driveService, file.id)
                    val remoteLoc = json.decodeFromString<Location>(content)
                    val remoteTime = file.modifiedTime?.value ?: remoteLoc.lastModified
                    val localLoc = repository.getAllLocationsSync().find { it.id == remoteLoc.id }

                    if (localLoc == null || remoteTime > localLoc.lastModified) {
                        repository.upsertLocation(remoteLoc.copy(lastModified = remoteTime))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private suspend fun pullItems(driveService: Drive, folderId: String) {
        val query = "'$folderId' in parents and trashed = false"
        val files = driveService.files().list().setQ(query).setFields("files(id, name, modifiedTime)").execute().files ?: return

        files.forEach { file ->
            if (file.name.endsWith(".json")) {
                try {
                    val content = readFile(driveService, file.id)
                    val remoteItem = json.decodeFromString<Item>(content)
                    val remoteTime = file.modifiedTime?.value ?: remoteItem.lastModified
                    val localItem = repository.getAllItemsSync().find { it.id == remoteItem.id }

                    if (localItem == null || remoteTime > localItem.lastModified) {
                        repository.upsertItem(remoteItem.copy(lastModified = remoteTime))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private suspend fun pushLocations(driveService: Drive, folderId: String) {
        val query = "'$folderId' in parents and trashed = false"
        val files = driveService.files().list().setQ(query).setFields("files(id, name, modifiedTime)").execute().files ?: emptyList()
        val fileMap = files.associateBy { it.name }

        repository.getAllLocationsSync().forEach { loc ->
            val fileName = "${loc.id}.json"
            val file = fileMap[fileName]
            
            var shouldPush = false
            if (file == null) {
                shouldPush = true
            } else {
                val remoteTime = file.modifiedTime?.value ?: 0L
                if (loc.lastModified > remoteTime) {
                    shouldPush = true
                }
            }

            if (shouldPush) {
                val content = json.encodeToString(loc)
                val byteArrayContent = ByteArrayContent.fromString("application/json", content)
                if (file == null) {
                    val fileMetadata = com.google.api.services.drive.model.File().apply {
                        this.name = fileName
                        this.parents = listOf(folderId)
                    }
                    driveService.files().create(fileMetadata, byteArrayContent).execute()
                } else {
                    driveService.files().update(file.id, null, byteArrayContent).execute()
                }
            }
        }
    }

    private suspend fun pushItems(driveService: Drive, folderId: String) {
        val query = "'$folderId' in parents and trashed = false"
        val files = driveService.files().list().setQ(query).setFields("files(id, name, modifiedTime)").execute().files ?: emptyList()
        val fileMap = files.associateBy { it.name }

        repository.getAllItemsSync().forEach { item ->
            val fileName = "${item.id}.json"
            val file = fileMap[fileName]

            var shouldPush = false
            if (file == null) {
                shouldPush = true
            } else {
                val remoteTime = file.modifiedTime?.value ?: 0L
                if (item.lastModified > remoteTime) {
                    shouldPush = true
                }
            }

            if (shouldPush) {
                val content = json.encodeToString(item)
                val byteArrayContent = ByteArrayContent.fromString("application/json", content)
                if (file == null) {
                    val fileMetadata = com.google.api.services.drive.model.File().apply {
                        this.name = fileName
                        this.parents = listOf(folderId)
                    }
                    driveService.files().create(fileMetadata, byteArrayContent).execute()
                } else {
                    driveService.files().update(file.id, null, byteArrayContent).execute()
                }
            }
        }
    }

    private suspend fun pushImages(driveService: Drive, folderId: String) {
        val query = "'$folderId' in parents and trashed = false"
        val files = driveService.files().list().setQ(query).setFields("files(name)").execute().files ?: emptyList()
        val remoteFiles = files.mapNotNull { it.name }.toSet()

        repository.getAllItemsSync().forEach { item ->
            item.imageUris.forEach { uriString ->
                val file = java.io.File(uriString)
                if (file.exists() && !remoteFiles.contains(file.name)) {
                    val fileMetadata = com.google.api.services.drive.model.File().apply {
                        this.name = file.name
                        this.parents = listOf(folderId)
                    }
                    val fileContent = FileContent("image/jpeg", file)
                    driveService.files().create(fileMetadata, fileContent).execute()
                }
            }
        }
    }

    private fun readFile(driveService: Drive, fileId: String): String {
        return driveService.files().get(fileId).executeMediaAsInputStream().use { input ->
            InputStreamReader(input).readText()
        }
    }
}
