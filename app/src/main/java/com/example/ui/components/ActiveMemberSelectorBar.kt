package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FamilyMember
import com.example.ui.theme.CinemaDarkBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.VelvetRuby

@Composable
fun ActiveMemberSelectorBar(
    activeMember: FamilyMember?,
    allMembers: List<FamilyMember>,
    pendingMemberForAuth: FamilyMember?,
    pinAuthError: String?,
    onRequestSelectMember: (FamilyMember) -> Unit,
    onSubmitPinAuth: (String) -> Unit,
    onCancelPinAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMemberPicker by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "CinéFamille",
                color = CinemaGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Ciné-club & envies partagées",
                color = CinemaTextSecondary,
                fontSize = 11.sp
            )
        }

        // Active profile chip (Only Admin can switch accounts)
        val isAdmin = activeMember?.isAdmin == true
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CinemaDarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdmin) CinemaGold else CinemaDarkBorder),
            modifier = Modifier
                .clickable(enabled = isAdmin) { showMemberPicker = true }
                .testTag("switch_active_member_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = activeMember?.colorHex?.let { Color(it) } ?: CinemaGold,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = activeMember?.avatarEmoji ?: "🎬",
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = activeMember?.name ?: "Choisir profil",
                    color = CinemaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Changer",
                        tint = CinemaGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Profile selection picker
    if (showMemberPicker) {
        Dialog(onDismissRequest = { showMemberPicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CinemaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Changer de profil",
                        color = CinemaTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chaque profil a un accès sécurisé par code PIN.",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allMembers.forEach { member ->
                            val isSelected = member.id == activeMember?.id
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CinemaGold.copy(alpha = 0.2f) else CinemaDarkSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CinemaGold else CinemaDarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMemberPicker = false
                                        onRequestSelectMember(member)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(member.colorHex),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = member.avatarEmoji, fontSize = 20.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.name,
                                            color = CinemaTextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = member.role,
                                            color = CinemaTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Sécurisé par code PIN",
                                        tint = CinemaGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // PIN Authentication Dialog
    if (pendingMemberForAuth != null) {
        Dialog(onDismissRequest = {
            enteredPin = ""
            onCancelPinAuth()
        }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CinemaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CinemaGold),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(pendingMemberForAuth.colorHex),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = pendingMemberForAuth.avatarEmoji, fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Accès sécurisé pour ${pendingMemberForAuth.name}",
                        color = CinemaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Entrez votre code PIN (Test: ${pendingMemberForAuth.pinCode})",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 6) enteredPin = it },
                        placeholder = { Text("Code PIN", color = CinemaTextSecondary) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
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
                            .testTag("pin_auth_input")
                    )

                    if (pinAuthError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pinAuthError,
                            color = VelvetRuby,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                enteredPin = ""
                                onCancelPinAuth()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler", color = CinemaTextSecondary)
                        }

                        Button(
                            onClick = {
                                onSubmitPinAuth(enteredPin)
                                enteredPin = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_pin_auth_button")
                        ) {
                            Text("Déverrouiller", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
