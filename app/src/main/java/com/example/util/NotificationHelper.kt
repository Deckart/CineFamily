package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.MovieNotification

object NotificationHelper {
    const val CHANNEL_ID = "channel_cine_famille_new_movies"
    const val CHANNEL_NAME = "Nouveaux films partagés"
    const val ACTION_JOIN_MOVIE = "com.example.ACTION_JOIN_MOVIE"
    const val EXTRA_MOVIE_ID = "EXTRA_MOVIE_ID"
    const val EXTRA_MEMBER_ID = "EXTRA_MEMBER_ID"
    const val EXTRA_MOVIE_TITLE = "EXTRA_MOVIE_TITLE"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications lors de l'ajout d'un film par un membre de la famille"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewMovieNotification(context: Context, notification: MovieNotification, currentMemberId: String) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Tap on notification opens the app to this movie
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_MOVIE_ID", notification.movieId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notification.movieId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Ça m'intéresse aussi" Action Button
        val joinIntent = Intent(ACTION_JOIN_MOVIE).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_MOVIE_ID, notification.movieId)
            putExtra(EXTRA_MEMBER_ID, currentMemberId)
            putExtra(EXTRA_MOVIE_TITLE, notification.movieTitle)
        }
        val joinPendingIntent = PendingIntent.getBroadcast(
            context,
            notification.movieId.hashCode(),
            joinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${notification.addedByMemberEmoji} Nouveau film : ${notification.movieTitle}")
            .setContentText("${notification.addedByMemberName} a ajouté « ${notification.movieTitle} » à la filmothèque !")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${notification.addedByMemberName} a partagé « ${notification.movieTitle} » (${notification.targetAudienceType}). Tapez pour voir la fiche ou cliquez ci-dessous pour rejoindre les spectateurs.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_input_add,
                "Ça m'intéresse aussi 🙋",
                joinPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(notification.movieId.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // Permission check safe fallback
        }
    }

    fun showJoinedConfirmationNotification(context: Context, movieTitle: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("✅ Rejoint avec succès !")
            .setContentText("Vous êtes maintenant inscrit(e) pour voir « $movieTitle ».")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(movieTitle.hashCode() + 1000, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
