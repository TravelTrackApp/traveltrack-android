package week11.st765512.finalproject.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import week11.st765512.finalproject.data.model.Result

/**
 * Base Firestore Repository class
 * Provides common CRUD operations for Firestore collections
 */
class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Get current user ID
     */
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * Create a document in a collection
     */
    suspend fun <T : Any> createDocument(
        collection: String,
        documentId: String? = null,
        data: T
    ): Result<String> {
        return try {
            val docRef = if (documentId != null) {
                firestore.collection(collection).document(documentId)
            } else {
                firestore.collection(collection).document()
            }
            
            docRef.set(data).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Read a document by ID
     */
    suspend fun <T> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Result<T?> {
        return try {
            val document = firestore.collection(collection)
                .document(documentId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.toObject(clazz)
                Result.Success(data)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update a document
     */
    suspend fun updateDocument(
        collection: String,
        documentId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection(collection)
                .document(documentId)
                .update(updates)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Delete a document
     */
    suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit> {
        return try {
            firestore.collection(collection)
                .document(documentId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get all documents from a collection
     */
    suspend fun <T> getAllDocuments(
        collection: String,
        clazz: Class<T>
    ): Result<List<T>> {
        return try {
            val snapshot = firestore.collection(collection)
                .get()
                .await()
            
            val documents = snapshot.documents.mapNotNull { doc ->
                doc.toObject(clazz)
            }
            Result.Success(documents)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Query documents with a field filter
     */
    suspend fun <T> queryDocuments(
        collection: String,
        field: String,
        value: Any,
        clazz: Class<T>
    ): Result<List<T>> {
        return try {
            val snapshot = firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()
            
            val documents = snapshot.documents.mapNotNull { doc ->
                doc.toObject(clazz)
            }
            Result.Success(documents)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get documents for current user
     */
    suspend fun <T> getUserDocuments(
        collection: String,
        userIdField: String = "userId",
        clazz: Class<T>
    ): Result<List<T>> {
        return currentUserId?.let { userId ->
            queryDocuments(collection, userIdField, userId, clazz)
        } ?: Result.Error(Exception("User not authenticated"))
    }

    /**
     * Create a document for current user
     */
    suspend fun <T : Any> createUserDocument(
        collection: String,
        documentId: String? = null,
        data: T,
        userIdField: String = "userId"
    ): Result<String> {
        return currentUserId?.let { userId ->
            val dataMap = if (data is Map<*, *>) {
                (data as Map<String, Any>).toMutableMap().apply {
                    put(userIdField, userId)
                }
            } else {
                // For data classes, you'll need to convert to map
                // This is a simplified version - you may need to use serialization
                throw UnsupportedOperationException("Use Map<String, Any> or implement custom serialization")
            }
            createDocument(collection, documentId, dataMap)
        } ?: Result.Error(Exception("User not authenticated"))
    }
}

