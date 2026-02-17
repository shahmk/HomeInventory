package com.brwnkid.homeinventory.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val locations: List<Location>,
    val items: List<Item>
)

class BackupRepository(
    private val context: Context,
    private val inventoryDao: InventoryDao
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    suspend fun performBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val items = inventoryDao.getAllItemsSync()
            val locations = inventoryDao.getAllLocationsSync()

            // Map items to export format (relative paths)
            val exportItems = items.map { item ->
                val newImageUris = item.imageUris.map { uriString ->
                    val file = File(uriString)
                    // Check if file is in app's internal storage
                    if (file.exists() && file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                        "images/${file.name}"
                    } else {
                        uriString 
                    }
                }
                item.copy(imageUris = newImageUris)
            }

            val backupData = BackupData(locations = locations, items = exportItems)
            val jsonString = json.encodeToString(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    
                    // Add JSON
                    val jsonEntry = ZipEntry("inventory.json")
                    zipOut.putNextEntry(jsonEntry)
                    zipOut.write(jsonString.toByteArray())
                    zipOut.closeEntry()

                    // Add Images
                    val alreadyAddedImages = mutableSetOf<String>()
                    
                    items.forEach { item ->
                        item.imageUris.forEach { uriString ->
                            val file = File(uriString)
                            if (file.exists() && file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                                val entryName = "images/${file.name}"
                                if (!alreadyAddedImages.contains(entryName)) {
                                    try {
                                        val entry = ZipEntry(entryName)
                                        zipOut.putNextEntry(entry)
                                        FileInputStream(file).use { input ->
                                            input.copyTo(zipOut)
                                        }
                                        zipOut.closeEntry()
                                        alreadyAddedImages.add(entryName)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun performRestore(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            var backupData: BackupData? = null

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val outFile = File(tempDir, entry.name)
                        // secure zip extraction check
                        if (!outFile.canonicalPath.startsWith(tempDir.canonicalPath)) {
                             throw SecurityException("Zip Path Traversal Vulnerability")
                        }
                        
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { output ->
                                zipIn.copyTo(output)
                            }
                        }
                        
                        if (entry.name == "inventory.json") {
                            val jsonString = outFile.readText()
                            backupData = json.decodeFromString<BackupData>(jsonString)
                        }
                        
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }

            if (backupData == null) {
                 tempDir.deleteRecursively()
                 return@withContext Result.failure(Exception("Invalid backup: inventory.json missing"))
            }

            backupData?.let { data ->
                // Restore Locations
                data.locations.forEach { location ->
                     inventoryDao.insertLocation(location)
                }

                // Restore Items
                data.items.forEach { item ->
                    val restoredImageUris = item.imageUris.map { uriString ->
                        if (uriString.startsWith("images/")) {
                            val fileName = File(uriString).name
                            val tempFile = File(tempDir, uriString) // images/foo.jpg -> tempDir/images/foo.jpg
                            if (tempFile.exists()) {
                                val destFile = File(context.filesDir, fileName)
                                tempFile.copyTo(destFile, overwrite = true)
                                destFile.absolutePath
                            } else {
                                uriString 
                            }
                        } else {
                            uriString
                        }
                    }
                    inventoryDao.insertItem(item.copy(imageUris = restoredImageUris))
                }
            }

            tempDir.deleteRecursively()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
