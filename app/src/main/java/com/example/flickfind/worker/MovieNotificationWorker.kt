package com.example.flickfind.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class MovieNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val movieTitle = inputData.getString("movie_title") ?: "Phim mới"
        val movieId = inputData.getInt("movie_id", 0)

        showNotification(movieTitle, movieId)

        return Result.success()
    }

    private fun showNotification(title: String, id: Int) {
        val channelId = "movie_release_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo công chiếu phim",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Phim đã công chiếu!")
            .setContentText("Bộ phim \"$title\" mà bạn yêu thích đã ra mắt hôm nay.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
