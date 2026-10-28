package com.example.data.model

import com.example.data.local.FamilyMember

data class DeletedMemberInfo(
    val member: FamilyMember,
    val deletedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val RETENTION_DAYS = 7L
        const val RETENTION_PERIOD_MILLIS = RETENTION_DAYS * 24 * 60 * 60 * 1000L
    }

    val isExpired: Boolean
        get() = (System.currentTimeMillis() - deletedAt) >= RETENTION_PERIOD_MILLIS

    val daysRemaining: Int
        get() {
            val elapsed = System.currentTimeMillis() - deletedAt
            val remaining = (RETENTION_PERIOD_MILLIS - elapsed).coerceAtLeast(0L)
            return kotlin.math.ceil(remaining.toDouble() / (24.0 * 60 * 60 * 1000.0)).toInt().coerceIn(0, 7)
        }

    val expiryLabel: String
        get() {
            val elapsed = System.currentTimeMillis() - deletedAt
            val remainingMillis = (RETENTION_PERIOD_MILLIS - elapsed).coerceAtLeast(0L)
            val totalHours = (remainingMillis / (60 * 60 * 1000L)).toInt()
            val days = daysRemaining
            return when {
                days > 1 -> "Expire dans $days jours"
                totalHours in 12..24 -> "Expire demain"
                totalHours > 0 -> "Expire dans ${totalHours}h"
                else -> "Expire très bientôt"
            }
        }
}
