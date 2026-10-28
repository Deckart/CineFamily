package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FamilyMember::class, Movie::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cine_famille_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.familyMemberDao(), database.movieDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(memberDao: FamilyMemberDao, movieDao: MovieDao) {
            val steph = FamilyMember(
                id = "steph",
                name = "Steph",
                avatarEmoji = "👑",
                colorHex = 0xFFFFB300,
                role = "Administrateur Foyer",
                pinCode = "0000",
                preferredGenres = "Tous genres, Comédie, Aventure, SF",
                isAdmin = true
            )
            val clara = FamilyMember(
                id = "clara",
                name = "Clara (C)",
                avatarEmoji = "🍿",
                colorHex = 0xFFFF4081,
                role = "Fan de Comédies & Romance",
                pinCode = "1111",
                preferredGenres = "Comédie, Romance, Animation",
                isAdmin = false
            )
            val antoine = FamilyMember(
                id = "antoine",
                name = "Antoine (A)",
                avatarEmoji = "🚀",
                colorHex = 0xFF448AFF,
                role = "SF & Aventure",
                pinCode = "2222",
                preferredGenres = "Science-fiction, Action, Aventure",
                isAdmin = false
            )
            val sophie = FamilyMember(
                id = "sophie",
                name = "Sophie (S)",
                avatarEmoji = "✨",
                colorHex = 0xFF00E676,
                role = "Animation & Famille",
                pinCode = "3333",
                preferredGenres = "Animation, Famille, Fantastique",
                isAdmin = false
            )

            memberDao.insertMembers(listOf(steph, clara, antoine, sophie))

            val initialMovies = listOf(
                Movie(
                    id = "movie_1",
                    title = "Retour vers le Futur",
                    originalTitle = "Back to the Future",
                    posterUrl = "https://m.media-amazon.com/images/M/MV5BMjA5NTYzMDMyM15BMl5BanBnXkFtZTgwNjU3NDU2MTE@._V1_SX300.jpg",
                    year = 1985,
                    durationMinutes = 116,
                    genres = "Science-fiction, Aventure, Comédie",
                    synopsis = "1985. Le jeune Marty McFly mène une existence anonyme auprès de sa famille. Ami de l'excentrique professeur Emmett Brown, il se retrouve propulsé en 1955 à bord d'une DeLorean modifiée.",
                    director = "Robert Zemeckis",
                    rating = 4.8f,
                    contentRating = "Tous publics",
                    addedByMemberId = "antoine",
                    addedByMemberName = "Antoine (A)",
                    addedByMemberEmoji = "🚀",
                    addedTimestamp = System.currentTimeMillis() - 86400000 * 5,
                    isWatched = false,
                    userNotes = "Un classique indémodable à regarder tous ensemble !",
                    familyVotes = 3,
                    targetAudienceType = "FAMILY",
                    allowedMemberIds = "all",
                    streamingServices = "Netflix, Amazon Prime Video",
                    reviewsJson = """[{"id":"r1","authorId":"antoine","authorName":"Antoine (A)","authorEmoji":"🚀","rating":5,"comment":"Un chef-d'œuvre absolu de SF !","timestamp":1700000000000},{"id":"r2","authorId":"sophie","authorName":"Sophie (S)","authorEmoji":"✨","rating":4,"comment":"Super drôle et captivant pour toute la famille.","timestamp":1700005000000}]"""
                ),
                Movie(
                    id = "movie_2",
                    title = "Spider-Man: New Generation",
                    originalTitle = "Spider-Man: Into the Spider-Verse",
                    posterUrl = "https://m.media-amazon.com/images/M/MV5BMjMwNDkxMTgyNV5BMl5BanBnXkFtZTgwNjkwNTM3NjM@._V1_SX300.jpg",
                    year = 2018,
                    durationMinutes = 117,
                    genres = "Animation, Action, Aventure",
                    synopsis = "Miles Morales, un adolescent de Brooklyn, est mordu par une araignée radioactive. Lorsqu'il découvre un portail vers d'autres dimensions, il rencontre plusieurs autres Spider-Héros.",
                    director = "Peter Ramsey",
                    rating = 4.9f,
                    contentRating = "Tous publics",
                    addedByMemberId = "antoine",
                    addedByMemberName = "Antoine (A)",
                    addedByMemberEmoji = "🚀",
                    addedTimestamp = System.currentTimeMillis() - 86400000 * 4,
                    isWatched = false,
                    userNotes = "Style graphique incroyable !",
                    familyVotes = 2,
                    targetAudienceType = "GROUP",
                    allowedMemberIds = "antoine,clara",
                    streamingServices = "Netflix",
                    reviewsJson = """[{"id":"r3","authorId":"clara","authorName":"Clara (C)","authorEmoji":"🍿","rating":5,"comment":"Visuellement incroyable, on ne s'ennuie pas une seconde.","timestamp":1700010000000}]"""
                ),
                Movie(
                    id = "movie_3",
                    title = "Coco",
                    originalTitle = "Coco",
                    posterUrl = "https://m.media-amazon.com/images/M/MV5BYjQ5NjM0Y2YtNjZkNC00ZDhkLWJjMWItN2QyNzFkZTFhMGI1XkEyXkFqcGdeQXVyNDQxNjcxNQ@@._V1_SX300.jpg",
                    year = 2017,
                    durationMinutes = 105,
                    genres = "Animation, Famille, Musique",
                    synopsis = "Depuis plusieurs générations, la musique est bannie dans la famille de Miguel. Pourtant, Miguel rêve de devenir un musicien aussi accompli que son idole.",
                    director = "Lee Unkrich",
                    rating = 4.8f,
                    contentRating = "Tous publics",
                    addedByMemberId = "sophie",
                    addedByMemberName = "Sophie (S)",
                    addedByMemberEmoji = "✨",
                    addedTimestamp = System.currentTimeMillis() - 86400000 * 3,
                    isWatched = false,
                    userNotes = "Préparez les mouchoirs pour la fin !",
                    familyVotes = 4,
                    targetAudienceType = "FAMILY",
                    allowedMemberIds = "all",
                    streamingServices = "Amazon Prime Video, Gratuit",
                    reviewsJson = """[{"id":"r4","authorId":"sophie","authorName":"Sophie (S)","authorEmoji":"✨","rating":5,"comment":"Magique et tellement émouvant.","timestamp":1700020000000}]"""
                ),
                Movie(
                    id = "movie_4",
                    title = "Le Fabuleux Destin d'Amélie Poulain",
                    originalTitle = "Le Fabuleux Destin d'Amélie Poulain",
                    posterUrl = "https://m.media-amazon.com/images/M/MV5BNDg4NjM1YjMtYmNhZC00MjM0LWFiZmFhZmY3ZmZhZDAwXkEyXkFqcGdeQXVyNDk3OD9Dg@@._V1_SX300.jpg",
                    year = 2001,
                    durationMinutes = 122,
                    genres = "Comédie, Romance",
                    synopsis = "Amélie, serveuse à Montmartre, décide de réparer la vie des gens qui l'entourent. Son quotidien est bousculé lorsqu'elle tombe amoureuse d'un garçon mystérieux.",
                    director = "Jean-Pierre Jeunet",
                    rating = 4.7f,
                    contentRating = "Tous publics",
                    addedByMemberId = "clara",
                    addedByMemberName = "Clara (C)",
                    addedByMemberEmoji = "🍿",
                    addedTimestamp = System.currentTimeMillis() - 86400000 * 2,
                    isWatched = false,
                    userNotes = "Une atmosphère douce pour une soirée cosy.",
                    familyVotes = 2,
                    targetAudienceType = "GROUP",
                    allowedMemberIds = "clara,sophie",
                    streamingServices = "Gratuit",
                    reviewsJson = """[{"id":"r5","authorId":"clara","authorName":"Clara (C)","authorEmoji":"🍿","rating":5,"comment":"Une douceur poétique inimitable.","timestamp":1700030000000}]"""
                ),
                Movie(
                    id = "movie_5",
                    title = "Interstellar",
                    originalTitle = "Interstellar",
                    posterUrl = "https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_SX300.jpg",
                    year = 2014,
                    durationMinutes = 169,
                    genres = "Science-fiction, Drame, Aventure",
                    synopsis = "Face à une Terre devenue inhabitable, un groupe d'explorateurs utilise une faille spatio-temporelle pour franchir les limites des voyages humains.",
                    director = "Christopher Nolan",
                    rating = 4.9f,
                    contentRating = "10+",
                    addedByMemberId = "antoine",
                    addedByMemberName = "Antoine (A)",
                    addedByMemberEmoji = "🚀",
                    addedTimestamp = System.currentTimeMillis() - 3600000 * 5,
                    isWatched = false,
                    userNotes = "Mon film préféré de science-fiction.",
                    familyVotes = 1,
                    targetAudienceType = "SOLO",
                    allowedMemberIds = "antoine",
                    streamingServices = "Amazon Prime Video",
                    reviewsJson = """[{"id":"r6","authorId":"antoine","authorName":"Antoine (A)","authorEmoji":"🚀","rating":5,"comment":"Un chef d'œuvre spatial et musical.","timestamp":1700040000000}]"""
                )
            )

            movieDao.insertMovies(initialMovies)
        }
    }
}
