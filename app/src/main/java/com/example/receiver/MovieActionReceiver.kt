package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.repository.MovieRepository
import com.example.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MovieActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_JOIN_MOVIE) {
            val movieId = intent.getStringExtra(NotificationHelper.EXTRA_MOVIE_ID) ?: return
            val movieTitle = intent.getStringExtra(NotificationHelper.EXTRA_MOVIE_TITLE) ?: "le film"
            val sessionManager = SessionManager(context)
            val currentMemberId = intent.getStringExtra(NotificationHelper.EXTRA_MEMBER_ID)
                ?: sessionManager.getLoggedInMemberId()
                ?: return

            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context, scope)
                    val repository = MovieRepository(
                        context = context,
                        movieDao = db.movieDao(),
                        memberDao = db.familyMemberDao()
                    )

                    repository.joinMovieViewers(movieId, currentMemberId)

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "Vous avez rejoint les spectateurs de « $movieTitle » !",
                            Toast.LENGTH_SHORT
                        ).show()
                        NotificationHelper.showJoinedConfirmationNotification(context, movieTitle)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
