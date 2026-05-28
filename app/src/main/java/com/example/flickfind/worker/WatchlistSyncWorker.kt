package com.example.flickfind.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flickfind.data.local.AppDatabase
import com.example.flickfind.data.local.MovieEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WatchlistSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return Result.success()
        val db = AppDatabase.getDatabase(applicationContext)
        val firestore = FirebaseFirestore.getInstance()

        return try {
            // Lấy toàn bộ danh sách từ local của user hiện tại
            val localWatchlist = db.movieDao().getAllWatchlistSync(currentUser.uid)

            // Đẩy toàn bộ lên Firestore để đồng bộ hóa hoàn toàn (Integrity)
            val batch = firestore.batch()
            val userWatchlistRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("watchlist")

            // 1. Chỉ thực hiện đẩy dữ liệu từ Local lên Cloud (Upsert)
            // Lấy dữ liệu từ Cloud về trước để merge
            val snapshot = userWatchlistRef.get().await()
            val remoteMap = snapshot.documents.associateBy { it.id }

            for (movie in localWatchlist) {
                val docId = movie.id.toString()
                val remoteDoc = remoteMap[docId]
                
                if (remoteDoc != null) {
                    val remoteIsWatched = remoteDoc.getBoolean("isWatched") ?: false
                    // Nếu trạng thái khác nhau, ưu tiên trạng thái "true" (đã xem) 
                    // hoặc có thể thêm timestamp để chọn cái mới nhất. 
                    // Ở đây ưu tiên logic: đã xem ở bất kỳ đâu thì là đã xem.
                    if (movie.isWatched != remoteIsWatched) {
                        val mergedIsWatched = movie.isWatched || remoteIsWatched
                        val updatedMovie = movie.copy(isWatched = mergedIsWatched)
                        batch.set(userWatchlistRef.document(docId), updatedMovie)
                        db.movieDao().updateWatchedStatus(movie.id, currentUser.uid, mergedIsWatched)
                    } else {
                        batch.set(userWatchlistRef.document(docId), movie)
                    }
                } else {
                    batch.set(userWatchlistRef.document(docId), movie)
                }
            }

            batch.commit().await()
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WatchlistSyncWorker", "Sync failed: ${e.message}")
            Result.retry() // Thử lại khi có mạng
        }
    }
}
