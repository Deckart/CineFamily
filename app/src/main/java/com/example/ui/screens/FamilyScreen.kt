package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.data.model.AppUpdateInfo
import com.example.data.model.DeletedMemberInfo
import com.example.data.remote.DownloadState
import java.io.File
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FamilyMember
import com.example.data.local.Movie
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaDarkBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.StarYellow
import com.example.ui.theme.VelvetRuby

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FamilyScreen(
    members: List<FamilyMember>,
    movies: List<Movie>,
    activeMember: FamilyMember?,
    originalAdminSession: FamilyMember? = null,
    appUpdateInfo: AppUpdateInfo? = null,
    currentVersionCode: Int = 1,
    currentVersionName: String = "1.0",
    downloadState: DownloadState = DownloadState.Idle,
    recoverableMembers: List<DeletedMemberInfo> = emptyList(),
    onRequestSelectMember: (FamilyMember) -> Unit,
    onAddNewMember: (name: String, emoji: String, colorHex: Long, role: String, pinCode: String, genres: String) -> Unit,
    onUpdateMemberByAdmin: (memberId: String, name: String, role: String, pinCode: String, genres: String, emoji: String, colorHex: Long) -> Unit,
    onImpersonateMember: (FamilyMember) -> Unit,
    onExitImpersonation: () -> Unit,
    onDeleteMember: (FamilyMember) -> Unit,
    onRestoreMember: (FamilyMember) -> Unit = {},
    onPurgeDeletedMember: (String) -> Unit = {},
    onUpdateGenres: (String) -> Unit,
    onCheckUpdateManually: () -> Unit = {},
    onStartDownloadUpdate: (url: String, targetVersionCode: Int) -> Unit = { _, _ -> },
    onInstallDownloadedApk: (File) -> Unit = {},
    onPublishAppUpdate: (versionCode: Int, versionName: String, releaseNotes: String, apkUrl: String, isMandatory: Boolean) -> Unit = { _, _, _, _, _ -> },
    cloudSyncStatus: String = "Connecté au Cloud",
    onForceSync: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = activeMember?.isAdmin == true

    if (isAdmin) {
        // ADMIN MODE: Steph can manage members, view PINs, add/delete accounts, edit PINs, act as other members
        AdminFamilyView(
            members = members,
            movies = movies,
            activeMember = activeMember,
            appUpdateInfo = appUpdateInfo,
            currentVersionCode = currentVersionCode,
            currentVersionName = currentVersionName,
            downloadState = downloadState,
            recoverableMembers = recoverableMembers,
            cloudSyncStatus = cloudSyncStatus,
            onForceSync = onForceSync,
            onRequestSelectMember = onRequestSelectMember,
            onAddNewMember = onAddNewMember,
            onUpdateMemberByAdmin = onUpdateMemberByAdmin,
            onImpersonateMember = onImpersonateMember,
            onDeleteMember = onDeleteMember,
            onRestoreMember = onRestoreMember,
            onPurgeDeletedMember = onPurgeDeletedMember,
            onCheckUpdateManually = onCheckUpdateManually,
            onStartDownloadUpdate = onStartDownloadUpdate,
            onInstallDownloadedApk = onInstallDownloadedApk,
            onPublishAppUpdate = onPublishAppUpdate,
            onLogout = onLogout,
            modifier = modifier
        )
    } else {
        // STANDARD USER MODE: "Mon Profil" with genre preferences and logout, hiding other accounts
        UserProfileView(
            member = activeMember,
            originalAdminSession = originalAdminSession,
            movies = movies,
            appUpdateInfo = appUpdateInfo,
            currentVersionCode = currentVersionCode,
            currentVersionName = currentVersionName,
            downloadState = downloadState,
            onUpdateGenres = onUpdateGenres,
            onExitImpersonation = onExitImpersonation,
            onCheckUpdateManually = onCheckUpdateManually,
            onStartDownloadUpdate = onStartDownloadUpdate,
            onInstallDownloadedApk = onInstallDownloadedApk,
            onLogout = onLogout,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminFamilyView(
    members: List<FamilyMember>,
    movies: List<Movie>,
    activeMember: FamilyMember?,
    appUpdateInfo: AppUpdateInfo? = null,
    currentVersionCode: Int = 1,
    currentVersionName: String = "1.0",
    downloadState: DownloadState = DownloadState.Idle,
    recoverableMembers: List<DeletedMemberInfo> = emptyList(),
    cloudSyncStatus: String = "Connecté au Cloud",
    onForceSync: () -> Unit = {},
    onRequestSelectMember: (FamilyMember) -> Unit,
    onAddNewMember: (name: String, emoji: String, colorHex: Long, role: String, pinCode: String, genres: String) -> Unit,
    onUpdateMemberByAdmin: (memberId: String, name: String, role: String, pinCode: String, genres: String, emoji: String, colorHex: Long) -> Unit,
    onImpersonateMember: (FamilyMember) -> Unit,
    onDeleteMember: (FamilyMember) -> Unit,
    onRestoreMember: (FamilyMember) -> Unit = {},
    onPurgeDeletedMember: (String) -> Unit = {},
    onCheckUpdateManually: () -> Unit = {},
    onStartDownloadUpdate: (url: String, targetVersionCode: Int) -> Unit = { _, _ -> },
    onInstallDownloadedApk: (File) -> Unit = {},
    onPublishAppUpdate: (versionCode: Int, versionName: String, releaseNotes: String, apkUrl: String, isMandatory: Boolean) -> Unit = { _, _, _, _, _ -> },
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMember?>(null) }
    var showDeployUpdateDialog by remember { mutableStateOf(false) }
    var showRestoreMemberDialog by remember { mutableStateOf(false) }

    val totalMovies = movies.size
    val watchedMovies = movies.count { it.isWatched }
    val toWatchMovies = movies.count { !it.isWatched }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Administration Foyer",
                        color = CinemaTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = CinemaGold) {
                        Text(
                            text = "ADMIN",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Gérez les comptes, codes PIN et accès du foyer",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CinemaDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clickable { showRestoreMemberDialog = true }
                        .testTag("open_restore_member_dialog")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (recoverableMembers.isNotEmpty()) "Restaurer (${recoverableMembers.size})" else "Restaurer",
                            color = CinemaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CinemaGold,
                    modifier = Modifier
                        .clickable { showAddMemberDialog = true }
                        .testTag("open_add_member_dialog")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Nouveau", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recoverable Accounts Banner
        if (recoverableMembers.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CinemaGold.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRestoreMemberDialog = true }
                    .testTag("recoverable_members_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CinemaGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "${recoverableMembers.size} compte(s) supprimé(s) (conservés 7 jours)",
                                color = CinemaGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val namesPreview = recoverableMembers.joinToString(", ") { "${it.member.name} (${it.expiryLabel})" }
                            Text(
                                text = "Cliquez pour restaurer : $namesPreview",
                                color = CinemaTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { showRestoreMemberDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("banner_restore_accounts_btn")
                    ) {
                        Text("Restaurer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats Dashboard
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "☁️ Base Firebase & Ciné-Club Synchronisé",
                    color = CinemaGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetric(label = "À voir", value = "$toWatchMovies", color = CinemaGold)
                    StatMetric(label = "Déjà vus", value = "$watchedMovies", color = Color(0xFF00E676))
                    StatMetric(label = "Total films", value = "$totalMovies", color = CinemaTextPrimary)
                }
            }
        }

        // Member Cards List (Admin only sees all members & PINs)
        Text(
            text = "Comptes de la famille & Sécurité :",
            color = CinemaTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        members.forEach { member ->
            val memberMovieCount = movies.count { it.addedByMemberId == member.id }
            val isActive = member.id == activeMember?.id

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) CinemaGold.copy(alpha = 0.12f) else CinemaDarkSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = if (isActive) CinemaGold else CinemaDarkBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(member.colorHex),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = member.avatarEmoji, fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.name,
                                    color = CinemaTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (member.isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(6.dp), color = CinemaGold) {
                                        Text(
                                            text = "Admin",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = member.role,
                                color = CinemaTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // PIN indicator (visible to Admin)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinemaDarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "PIN: ${member.pinCode}", color = CinemaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (!member.isAdmin && members.size > 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onDeleteMember(member) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer membre", tint = VelvetRuby.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preferred Genres & Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Genres : ${member.preferredGenres}",
                            color = CinemaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$memberMovieCount films",
                            color = CinemaTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: "Modifier / PIN" and "Agir en tant que..."
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingMember = member },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("edit_member_btn_${member.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier / PIN", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (!isActive) {
                            Button(
                                onClick = { onImpersonateMember(member) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(38.dp)
                                    .testTag("act_as_member_btn_${member.id}")
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Agir en tant que ${member.name}",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CinemaDarkSurfaceVariant,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Profil connecté", color = CinemaTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloud Sync Status Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("admin_cloud_sync_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CinemaGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("☁️", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Synchronisation Cloud",
                                color = CinemaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cloudSyncStatus,
                                color = if (cloudSyncStatus.contains("Erreur") || cloudSyncStatus.contains("Hors ligne")) VelvetRuby else CinemaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = onForceSync,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaDarkSurfaceVariant,
                            contentColor = CinemaGold
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_force_cloud_sync")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp), tint = CinemaGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Synchroniser", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vos listes de films, comptes de la famille et soirées planifiées sont partagés instantanément entre tous les téléphones connectés.",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // App Updates Deployment Card (Admin Only)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("admin_updates_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CinemaGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Mises à jour de l'application",
                                color = CinemaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Installée : v$currentVersionName (code $currentVersionCode)",
                                color = CinemaTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (appUpdateInfo != null && appUpdateInfo.latestVersionCode > currentVersionCode) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinemaGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "v${appUpdateInfo.latestVersionName} active",
                                color = CinemaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Diffusez une nouvelle version à toute la famille. Dès que vous publiez l'APK, un message s'affiche sur leurs appareils pour installer la mise à jour d'un clic.",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCheckUpdateManually,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("admin_check_update_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vérifier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showDeployUpdateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        modifier = Modifier.weight(1.3f).height(40.dp).testTag("open_publish_update_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Diffuser mise à jour", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Logout
        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VelvetRuby),
            border = androidx.compose.foundation.BorderStroke(1.dp, VelvetRuby.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter de la session Admin")
        }
    }

    // Deploy Update Dialog
    if (showDeployUpdateDialog) {
        val defaultUrl = appUpdateInfo?.apkUrl?.takeIf { it.isNotBlank() && !it.startsWith("demo", ignoreCase = true) }
            ?: com.example.data.repository.MovieRepository.PERMANENT_APK_URL
        var targetCode by remember { mutableStateOf((appUpdateInfo?.latestVersionCode?.coerceAtLeast(currentVersionCode) ?: currentVersionCode).toString()) }
        var targetName by remember { mutableStateOf(appUpdateInfo?.latestVersionName ?: currentVersionName) }
        var targetNotes by remember { mutableStateOf(appUpdateInfo?.releaseNotes?.ifBlank { "Mise à jour v$currentVersionName : sélecteur par genre et durée (< 2h / > 2h), et optimisations de fluidité." } ?: "Mise à jour v$currentVersionName : sélecteur par genre et durée (< 2h / > 2h), et optimisations de fluidité.") }
        var targetUrl by remember { mutableStateOf(defaultUrl) }
        var isMandatory by remember { mutableStateOf(appUpdateInfo?.isMandatory ?: false) }

        Dialog(onDismissRequest = { showDeployUpdateDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CinemaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diffuser une mise à jour",
                            color = CinemaTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showDeployUpdateDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = CinemaTextSecondary)
                        }
                    }

                    Text(
                        text = "Les téléphones de la famille recevront automatiquement le bouton pour télécharger et installer cette version.",
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = targetName,
                        onValueChange = { targetName = it },
                        label = { Text("Nom de version (ex: 1.1)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("update_version_name_field")
                    )

                    OutlinedTextField(
                        value = targetCode,
                        onValueChange = { targetCode = it },
                        label = { Text("Code de version (ex: 2)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("update_version_code_field")
                    )

                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        label = { Text("Lien APK direct (ou 'demo' pour tester)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("update_apk_url_field")
                    )

                    Text(
                        text = "💡 Format permanent recommandé : https://github.com/<compte>/<repo>/releases/latest/download/CineFamily.apk\n(Évitez les liens temporaires objects.githubusercontent.com qui expirent au bout de 5 minutes avec une erreur 618).",
                        color = CinemaGold.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    OutlinedTextField(
                        value = targetNotes,
                        onValueChange = { targetNotes = it },
                        label = { Text("Nouveautés de cette version") },
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("update_notes_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Mise à jour obligatoire", color = CinemaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Exige la mise à jour pour utiliser l'app", color = CinemaTextSecondary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = isMandatory,
                            onCheckedChange = { isMandatory = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CinemaGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val code = targetCode.toIntOrNull() ?: (currentVersionCode + 1)
                            onPublishAppUpdate(code, targetName, targetNotes, targetUrl, isMandatory)
                            showDeployUpdateDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("confirm_publish_update_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Diffuser à toute la famille", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Restore Member Dialog
    if (showRestoreMemberDialog) {
        Dialog(onDismissRequest = { showRestoreMemberDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CinemaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = CinemaGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Restore, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = "Restaurer un compte",
                                color = CinemaTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showRestoreMemberDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = CinemaTextSecondary)
                        }
                    }

                    Text(
                        text = "Les comptes supprimés sont conservés pendant 7 jours avant d'être purgés définitivement. Durant ce délai, vous pouvez les restaurer ou forcer une suppression immédiate.",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    if (recoverableMembers.isNotEmpty()) {
                        Text(
                            text = "Comptes supprimés récupérables (délai 7 jours) :",
                            color = CinemaGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        for (recInfo in recoverableMembers) {
                            val recMember = recInfo.member
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CinemaDarkSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                                modifier = Modifier.fillMaxWidth().testTag("recoverable_card_${recMember.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(recMember.colorHex),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = recMember.avatarEmoji, fontSize = 20.sp)
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = recMember.name,
                                                color = CinemaTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${recMember.role} • PIN: ${recMember.pinCode}",
                                                color = CinemaTextSecondary,
                                                fontSize = 11.sp
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (recInfo.daysRemaining <= 1) VelvetRuby.copy(alpha = 0.2f) else CinemaGold.copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(0.5.dp, if (recInfo.daysRemaining <= 1) VelvetRuby else CinemaGold.copy(alpha = 0.4f)),
                                                modifier = Modifier.padding(top = 3.dp)
                                            ) {
                                                Text(
                                                    text = "⏳ ${recInfo.expiryLabel}",
                                                    color = if (recInfo.daysRemaining <= 1) VelvetRuby else CinemaGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                onPurgeDeletedMember(recMember.id)
                                            },
                                            modifier = Modifier.size(36.dp).testTag("purge_member_btn_${recMember.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Purger définitivement",
                                                tint = VelvetRuby.copy(alpha = 0.85f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                onRestoreMember(recMember)
                                                showRestoreMemberDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("restore_member_btn_${recMember.id}")
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Restaurer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CinemaDarkSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aucun compte supprimé récemment détecté.",
                                color = CinemaTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            showRestoreMemberDialog = false
                            showAddMemberDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("custom_restore_or_create_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Créer ou restaurer un autre profil", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        var newName by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf("Cinéphile") }
        var newPin by remember { mutableStateOf("1234") }
        var newGenres by remember { mutableStateOf("Animation, Comédie, Aventure") }

        val emojis = listOf("🍿", "🎬", "🚀", "✨", "🦁", "🍕", "🎮", "🎸")
        val colors = listOf(0xFFFF4081, 0xFF448AFF, 0xFF00E676, 0xFFFF9100, 0xFFB388FF)

        var selectedEmoji by remember { mutableStateOf(emojis.first()) }
        var selectedColor by remember { mutableStateOf(colors.first()) }

        Dialog(onDismissRequest = { showAddMemberDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CinemaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nouveau profil sécurisé",
                        color = CinemaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Prénom") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        label = { Text("Code PIN de sécurité (4 chiffres)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newGenres,
                        onValueChange = { newGenres = it },
                        label = { Text("Préférences de genres") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Avatar :", color = CinemaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(emojis) { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CinemaGold else CinemaDarkSurfaceVariant,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onAddNewMember(newName, selectedEmoji, selectedColor, newRole, newPin, newGenres)
                                showAddMemberDialog = false
                            }
                        },
                        enabled = newName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Créer le compte", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit Member Dialog (Allows Admin to change PIN, role, name, genres, avatar, etc.)
    if (editingMember != null) {
        val target = editingMember!!
        var editName by remember(target) { mutableStateOf(target.name) }
        var editRole by remember(target) { mutableStateOf(target.role) }
        var editPin by remember(target) { mutableStateOf(target.pinCode) }
        var editGenres by remember(target) { mutableStateOf(target.preferredGenres) }
        val emojis = listOf("🍿", "🎬", "🚀", "✨", "🦁", "🍕", "🎮", "🎸", "👑", "🎯")
        val colors = listOf(0xFFFF4081, 0xFF448AFF, 0xFF00E676, 0xFFFF9100, 0xFFB388FF, 0xFFFFD700)
        var selectedEmoji by remember(target) { mutableStateOf(target.avatarEmoji) }
        var selectedColor by remember(target) { mutableStateOf(target.colorHex) }

        Dialog(onDismissRequest = { editingMember = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CinemaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modifier le profil de ${target.name}",
                            color = CinemaTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { editingMember = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = CinemaTextSecondary)
                        }
                    }

                    // Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Prénom / Nom") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_member_name_field")
                    )

                    // Role
                    OutlinedTextField(
                        value = editRole,
                        onValueChange = { editRole = it },
                        label = { Text("Rôle (ex: Maman, Ado, Enfant)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_member_role_field")
                    )

                    // PIN Code - CRITICAL FEATURE
                    OutlinedTextField(
                        value = editPin,
                        onValueChange = { if (it.length <= 4) editPin = it },
                        label = { Text("Code PIN de sécurité (4 chiffres)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CinemaGold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_member_pin_field")
                    )

                    // Genres
                    OutlinedTextField(
                        value = editGenres,
                        onValueChange = { editGenres = it },
                        label = { Text("Préférences de genres") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = CinemaDarkBorder,
                            focusedTextColor = CinemaTextPrimary,
                            unfocusedTextColor = CinemaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Avatar :", color = CinemaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(emojis) { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CinemaGold else CinemaDarkSurfaceVariant,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Text("Couleur :", color = CinemaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colors) { colorHex ->
                            val isSelected = selectedColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(colorHex), CircleShape)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorHex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (editName.isNotBlank() && editPin.isNotBlank()) {
                                onUpdateMemberByAdmin(
                                    target.id,
                                    editName,
                                    editRole,
                                    editPin,
                                    editGenres,
                                    selectedEmoji,
                                    selectedColor
                                )
                                editingMember = null
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_edit_member_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer les modifications", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    // Impersonate Button inside edit modal
                    if (target.id != activeMember?.id) {
                        OutlinedButton(
                            onClick = {
                                onImpersonateMember(target)
                                editingMember = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("act_as_from_dialog_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agir en tant que ${target.name}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileView(
    member: FamilyMember?,
    originalAdminSession: FamilyMember? = null,
    movies: List<Movie>,
    appUpdateInfo: AppUpdateInfo? = null,
    currentVersionCode: Int = 1,
    currentVersionName: String = "1.0",
    downloadState: DownloadState = DownloadState.Idle,
    onUpdateGenres: (String) -> Unit,
    onExitImpersonation: () -> Unit,
    onCheckUpdateManually: () -> Unit = {},
    onStartDownloadUpdate: (url: String, targetVersionCode: Int) -> Unit = { _, _ -> },
    onInstallDownloadedApk: (File) -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (member == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune session active", color = CinemaTextSecondary)
        }
        return
    }

    var genresText by remember(member.preferredGenres) { mutableStateOf(member.preferredGenres) }
    var isSavedFeedback by remember { mutableStateOf(false) }

    val myAddedMovies = movies.count { it.addedByMemberId == member.id }
    val commonGenres = listOf(
        "Animation", "Comédie", "Aventure", "Science-fiction",
        "Action", "Famille", "Drame", "Thriller", "Fantastique"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sticky Impersonation Warning Banner inside UserProfileView if Admin is impersonating
        if (originalAdminSession != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaGold.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold),
                modifier = Modifier.fillMaxWidth().testTag("profile_impersonation_card")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Admin actif",
                            color = CinemaGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vous agissez sur le profil de ${member.name}.",
                            color = CinemaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onExitImpersonation,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        modifier = Modifier.testTag("exit_impersonation_profile_btn")
                    ) {
                        Text("Revenir à ${originalAdminSession.name}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Header
        Text(
            text = "Mon Profil",
            color = CinemaTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Member Identity Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CinemaGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(member.colorHex),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = member.avatarEmoji, fontSize = 34.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = member.name,
                    color = CinemaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = member.role,
                    color = CinemaTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetric(label = "Mes films ajoutés", value = "$myAddedMovies", color = CinemaGold)
                    StatMetric(label = "Statut", value = "Sécurisé", color = Color(0xFF00E676))
                }
            }
        }

        // Genre Preferences Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎯 Mes Préférences de Genres",
                    color = CinemaGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Le tirage aléatoire et le moteur de recommandation prennent en compte vos goûts.",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion chips
                Text(
                    text = "Ajouter rapidement un genre :",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonGenres.forEach { g ->
                        val isSelected = genresText.contains(g, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CinemaGold.copy(alpha = 0.2f) else CinemaDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CinemaGold else CinemaDarkBorder
                            ),
                            modifier = Modifier.clickable {
                                val currentList = genresText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                if (currentList.any { it.equals(g, ignoreCase = true) }) {
                                    currentList.removeAll { it.equals(g, ignoreCase = true) }
                                } else {
                                    currentList.add(g)
                                }
                                genresText = currentList.joinToString(", ")
                                isSavedFeedback = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = g,
                                    color = if (isSelected) CinemaGold else CinemaTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = genresText,
                    onValueChange = {
                        genresText = it
                        isSavedFeedback = false
                    },
                    label = { Text("Genres favoris (séparés par des virgules)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CinemaDarkSurfaceVariant,
                        unfocusedContainerColor = CinemaDarkSurfaceVariant,
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = CinemaDarkBorder,
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_genres_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onUpdateGenres(genresText)
                        isSavedFeedback = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_genres_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSavedFeedback) "Préférences enregistrées !" else "Enregistrer mes préférences",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // App Updates Card
        val isNewUpdate = appUpdateInfo != null && appUpdateInfo.latestVersionCode > currentVersionCode
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isNewUpdate) CinemaGold else CinemaDarkBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("user_profile_updates_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CinemaGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Mises à jour de CinéFamille",
                                color = CinemaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version installée : v$currentVersionName",
                                color = CinemaTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isNewUpdate) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CinemaGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "v${appUpdateInfo!!.latestVersionName}",
                                color = CinemaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isNewUpdate) {
                    Text(
                        text = "🎉 Nouvelle version v${appUpdateInfo!!.latestVersionName} disponible !",
                        color = CinemaGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (appUpdateInfo.releaseNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appUpdateInfo.releaseNotes,
                            color = CinemaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onStartDownloadUpdate(appUpdateInfo.apkUrl, appUpdateInfo.latestVersionCode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("user_download_update_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Télécharger et installer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Votre application est à jour. Vous bénéficiez de toutes les dernières nouveautés.",
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onCheckUpdateManually,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("user_check_update_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rechercher une mise à jour", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Security / Logout
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔒 Sécurité & Session",
                    color = CinemaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Votre session reste active sur cet appareil. Pour prêter votre appareil ou changer de profil, déconnectez-vous.",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VelvetRuby),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VelvetRuby.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_logout_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = VelvetRuby, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Se déconnecter de mon profil (${member.name})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = CinemaTextSecondary,
            fontSize = 11.sp
        )
    }
}
