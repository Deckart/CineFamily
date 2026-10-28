package com.example

import com.example.data.local.Movie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun movieFilter_byAudienceGroup_matchesTargetViewers() {
        val movies = listOf(
            Movie(id = "m1", title = "Film Famille", targetAudienceType = "FAMILY", allowedMemberIds = "all"),
            Movie(id = "m2", title = "Film Clara et Sophie", targetAudienceType = "GROUP", allowedMemberIds = "clara,sophie"),
            Movie(id = "m3", title = "Film Antoine et Clara", targetAudienceType = "GROUP", allowedMemberIds = "antoine,clara"),
            Movie(id = "m4", title = "Film Solo Antoine", targetAudienceType = "SOLO", addedByMemberId = "antoine")
        )

        // Case 1: C + S selected (Clara + Sophie)
        val viewersCS = setOf("clara", "sophie")
        val matchingCS = movies.filter { movie ->
            when (movie.targetAudienceType) {
                "FAMILY" -> true
                "SOLO" -> viewersCS.size == 1 && viewersCS.contains(movie.addedByMemberId)
                "GROUP" -> {
                    val allowed = movie.allowedMemberIds.split(",").toSet()
                    viewersCS.all { allowed.contains(it) }
                }
                else -> true
            }
        }
        // m1 (Family) and m2 (C+S) match
        assertEquals(2, matchingCS.size)
        assertTrue(matchingCS.any { it.id == "m1" })
        assertTrue(matchingCS.any { it.id == "m2" })

        // Case 2: A + C selected (Antoine + Clara)
        val viewersAC = setOf("antoine", "clara")
        val matchingAC = movies.filter { movie ->
            when (movie.targetAudienceType) {
                "FAMILY" -> true
                "SOLO" -> viewersAC.size == 1 && viewersAC.contains(movie.addedByMemberId)
                "GROUP" -> {
                    val allowed = movie.allowedMemberIds.split(",").toSet()
                    viewersAC.all { allowed.contains(it) }
                }
                else -> true
            }
        }
        // m1 (Family) and m3 (A+C) match
        assertEquals(2, matchingAC.size)
        assertTrue(matchingAC.any { it.id == "m3" })
    }

    @Test
    fun movieFilter_byStreamingPlatform_filtersCorrectly() {
        val movies = listOf(
            Movie(id = "1", title = "Netflix Film", streamingServices = "Netflix"),
            Movie(id = "2", title = "Prime Film", streamingServices = "Amazon Prime Video"),
            Movie(id = "3", title = "Gratuit Film", streamingServices = "Gratuit")
        )

        val netflixMovies = movies.filter { it.streamingServices.contains("Netflix") }
        assertEquals(1, netflixMovies.size)
        assertEquals("Netflix Film", netflixMovies[0].title)
    }

    @Test
    fun memberInterest_toggleJoinAndLeave_updatesGroupAudience() {
        val initialMovie = Movie(
            id = "m_test",
            title = "Film Test A+C",
            targetAudienceType = "GROUP",
            allowedMemberIds = "antoine,clara",
            familyVotes = 2
        )

        // 1. Sophie (S) adds herself
        val currentIds = initialMovie.allowedMemberIds.split(",").toSet()
        val afterJoinIds = currentIds + "sophie"
        val joinedMovie = initialMovie.copy(
            allowedMemberIds = afterJoinIds.joinToString(","),
            familyVotes = initialMovie.familyVotes + 1
        )

        assertTrue(joinedMovie.allowedMemberIds.contains("sophie"))
        assertTrue(joinedMovie.allowedMemberIds.contains("antoine"))
        assertTrue(joinedMovie.allowedMemberIds.contains("clara"))
        assertEquals(3, joinedMovie.familyVotes)

        // Verify eligibility for C+A+S
        val viewersCAS = setOf("clara", "antoine", "sophie")
        val allowedCAS = joinedMovie.allowedMemberIds.split(",").toSet()
        assertTrue(viewersCAS.all { allowedCAS.contains(it) })

        // 2. Sophie (S) removes herself
        val afterLeaveIds = joinedMovie.allowedMemberIds.split(",").toSet() - "sophie"
        val leftMovie = joinedMovie.copy(
            allowedMemberIds = afterLeaveIds.joinToString(","),
            familyVotes = joinedMovie.familyVotes - 1
        )

        assertTrue(!leftMovie.allowedMemberIds.contains("sophie"))
        assertTrue(leftMovie.allowedMemberIds.contains("antoine"))
        assertTrue(leftMovie.allowedMemberIds.contains("clara"))
        assertEquals(2, leftMovie.familyVotes)
    }

    @Test
    fun appUpdate_versionComparison_detectsAvailableUpdate() {
        val currentVersionCode = 1
        val updateInfoAvailable = com.example.data.model.AppUpdateInfo(
            latestVersionCode = 2,
            latestVersionName = "1.1",
            releaseNotes = "Ajout de la sélection multi-spectateurs et correctifs",
            apkUrl = "https://example.com/cinefamille_v2.apk"
        )
        val updateInfoSame = com.example.data.model.AppUpdateInfo(
            latestVersionCode = 1,
            latestVersionName = "1.0",
            releaseNotes = "",
            apkUrl = ""
        )

        val isAvailable = updateInfoAvailable.latestVersionCode > currentVersionCode
        val isSameAvailable = updateInfoSame.latestVersionCode > currentVersionCode

        assertTrue(isAvailable)
        assertTrue(!isSameAvailable)
        assertEquals("1.1", updateInfoAvailable.latestVersionName)
    }

    @Test
    fun memberRestoration_detectsDeletedMember_andRestoresSuccessfully() {
        // Active list with Clara deleted
        val activeMembers = listOf(
            com.example.data.local.FamilyMember(id = "steph", name = "Steph"),
            com.example.data.local.FamilyMember(id = "antoine", name = "Antoine (A)")
        )
        val activeIds = activeMembers.map { it.id }.toSet()

        // Recoverable detection combines default members and deleted members
        val recoverable = com.example.data.local.DEFAULT_FAMILY_MEMBERS.filter { !activeIds.contains(it.id) }

        // Clara and Sophie should be detectable for restoration
        assertEquals(2, recoverable.size)
        assertTrue(recoverable.any { it.id == "clara" })
        assertTrue(recoverable.any { it.id == "sophie" })

        // Simulate restore of Clara
        val clara = recoverable.first { it.id == "clara" }
        val restoredList = activeMembers + clara

        assertEquals(3, restoredList.size)
        assertTrue(restoredList.any { it.id == "clara" && it.name == "Clara (C)" && it.pinCode == "1111" })
    }

    @Test
    fun deletedMember_within7Days_isRecoverable() {
        val now = System.currentTimeMillis()
        val member = com.example.data.local.FamilyMember(id = "clara", name = "Clara (C)")
        // Deleted 2 days ago
        val deletedInfo = com.example.data.model.DeletedMemberInfo(
            member = member,
            deletedAt = now - (2L * 24 * 60 * 60 * 1000L)
        )

        org.junit.Assert.assertFalse("Account deleted 2 days ago should not be expired", deletedInfo.isExpired)
        assertEquals(5, deletedInfo.daysRemaining)
        assertEquals("Expire dans 5 jours", deletedInfo.expiryLabel)
    }

    @Test
    fun deletedMember_olderThan7Days_isExpiredAndExcluded() {
        val now = System.currentTimeMillis()
        val recentMember = com.example.data.local.FamilyMember(id = "clara", name = "Clara (C)")
        val expiredMember = com.example.data.local.FamilyMember(id = "old_user", name = "Ancien Membre")

        val deletedList = listOf(
            com.example.data.model.DeletedMemberInfo(
                member = recentMember,
                deletedAt = now - (3L * 24 * 60 * 60 * 1000L) // 3 days ago
            ),
            com.example.data.model.DeletedMemberInfo(
                member = expiredMember,
                deletedAt = now - (8L * 24 * 60 * 60 * 1000L) // 8 days ago -> expired!
            )
        )

        assertTrue("Account deleted 8 days ago must be expired", deletedList[1].isExpired)
        org.junit.Assert.assertFalse("Account deleted 3 days ago must not be expired", deletedList[0].isExpired)

        // Filter valid recoverable accounts (under 7 days retention)
        val activeRecoverable = deletedList.filter { !it.isExpired }
        assertEquals(1, activeRecoverable.size)
        assertEquals("clara", activeRecoverable[0].member.id)
    }
}
