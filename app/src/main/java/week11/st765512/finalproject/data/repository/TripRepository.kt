package week11.st765512.finalproject.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st765512.finalproject.data.model.Result
import week11.st765512.finalproject.data.model.Trip

class TripRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun tripsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("trips")

    fun observeTrips(userId: String): Flow<Result<List<Trip>>> = callbackFlow {
        val registration = tripsCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> trySend(Result.Error(error))
                    snapshot == null -> trySend(Result.Success(emptyList()))
                    else -> {
                        val trips = snapshot.documents.mapNotNull { document ->
                            document.toObject(Trip::class.java)?.copy(id = document.id)
                        }
                        trySend(Result.Success(trips))
                    }
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun createTrip(userId: String, trip: Trip): Result<String> {
        return try {
            val docRef = tripsCollection(userId).document()
            val payload = trip.copy(
                id = docRef.id,
                userId = userId,
                createdAt = trip.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            )
            // Use set() to save the trip
            docRef.set(payload).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Log.e("TripRepository", "Error creating trip: ${e.message}", e)
            Result.Error(e)
        }
    }

    suspend fun deleteTrip(userId: String, tripId: String): Result<Unit> {
        return try {
            tripsCollection(userId).document(tripId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTrip(userId: String, tripId: String): Result<Trip?> {
        return try {
            val snapshot = tripsCollection(userId).document(tripId).get().await()
            val trip = if (snapshot.exists()) {
                snapshot.toObject(Trip::class.java)?.copy(id = snapshot.id)
            } else null
            Result.Success(trip)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateTrip(
        userId: String,
        tripId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            tripsCollection(userId).document(tripId).update(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

