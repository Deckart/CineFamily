package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val avatarEmoji: String = "🎬",
    val colorHex: Long = 0xFF448AFF,
    val role: String = "Famille",
    val pinCode: String = "1234",
    val preferredGenres: String = "Animation, Comédie",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

val DEFAULT_FAMILY_MEMBERS = listOf(
    FamilyMember(
        id = "steph",
        name = "Steph",
        avatarEmoji = "👑",
        colorHex = 0xFFFFB300,
        role = "Administrateur Foyer",
        pinCode = "0000",
        preferredGenres = "Tous genres, Comédie, Aventure, SF",
        isAdmin = true
    ),
    FamilyMember(
        id = "clara",
        name = "Clara (C)",
        avatarEmoji = "🍿",
        colorHex = 0xFFFF4081,
        role = "Fan de Comédies & Romance",
        pinCode = "1111",
        preferredGenres = "Comédie, Romance, Animation",
        isAdmin = false
    ),
    FamilyMember(
        id = "antoine",
        name = "Antoine (A)",
        avatarEmoji = "🚀",
        colorHex = 0xFF448AFF,
        role = "SF & Aventure",
        pinCode = "2222",
        preferredGenres = "Science-fiction, Action, Aventure",
        isAdmin = false
    ),
    FamilyMember(
        id = "sophie",
        name = "Sophie (S)",
        avatarEmoji = "✨",
        colorHex = 0xFF00E676,
        role = "Animation & Famille",
        pinCode = "3333",
        preferredGenres = "Animation, Famille, Fantastique",
        isAdmin = false
    )
)
