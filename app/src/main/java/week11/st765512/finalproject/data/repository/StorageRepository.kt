package week11.st765512.finalproject.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import week11.st765512.finalproject.data.model.Result
import java.io.InputStream

/**
 * Repository for Firebase Storage operations
 */
class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /**
     * Get current user ID
     */
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * Upload image to Firebase Storage
     * @param imageUri Local URI of the image (can be content:// or file://)
     * @param context Context needed to open content URI
     * @param folderName Folder name in storage (e.g., "trip_photos")
     * @return Result containing the download URL
     */
    suspend fun uploadImage(
        imageUri: Uri,
        context: Context,
        folderName: String = "trip_photos"
    ): Result<String> {
        val userId = currentUserId ?: return Result.Error(Exception("User not authenticated"))
        
        return try {
            // Create unique filename
            val fileName = "${userId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child(folderName).child(fileName)
            
            // Handle content URI (from file picker) vs file URI
            val uploadTask = if (imageUri.scheme == "content") {
                // Use ContentResolver to open input stream for content URIs
                // We need to read the stream synchronously and upload it
                val inputStream = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(imageUri)
                } ?: return Result.Error(Exception("Cannot open input stream from URI"))
                
                try {
                    // putStream will read the entire stream, so we need to ensure it's available
                    storageRef.putStream(inputStream)
                } catch (e: Exception) {
                    inputStream.close()
                    throw e
                }
            } else {
                // Use putFile for file:// URIs
                storageRef.putFile(imageUri)
            }
            
            // Wait for upload to complete
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await()
            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Upload multiple images
     */
    suspend fun uploadImages(
        imageUris: List<Uri>,
        context: Context,
        folderName: String = "trip_photos"
    ): Result<List<String>> {
        val userId = currentUserId ?: return Result.Error(Exception("User not authenticated"))
        
        return try {
            val downloadUrls = mutableListOf<String>()
            for (uri in imageUris) {
                when (val result = uploadImage(uri, context, folderName)) {
                    is Result.Success -> downloadUrls.add(result.data)
                    is Result.Error -> return result
                    Result.Loading -> Unit
                }
            }
            Result.Success(downloadUrls)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Delete image from Firebase Storage
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

