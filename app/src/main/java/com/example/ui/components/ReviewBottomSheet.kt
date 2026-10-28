package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FamilyMember
import com.example.data.local.Movie
import com.example.ui.theme.CinemaDarkBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.StarYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    movie: Movie,
    activeMember: FamilyMember?,
    onDismiss: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    val ratingLabels = listOf(
        "1" to "À éviter",
        "2" to "Moyen",
        "3" to "Pas mal",
        "4" to "Très bon",
        "5" to "Chef-d'œuvre !"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CinemaDarkSurface,
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = CinemaDarkBorder,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
            ) {}
        },
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .testTag("review_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "Votre avis sur le film",
                color = CinemaGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = movie.title,
                color = CinemaTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Rating Stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$star étoiles",
                        tint = if (star <= rating) StarYellow else CinemaTextSecondary,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { rating = star }
                            .padding(horizontal = 2.dp)
                            .testTag("sheet_star_$star")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$rating/5 : ${ratingLabels.getOrNull(rating - 1)?.second ?: ""}",
                color = StarYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text Input with full IME keyboard clearance
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Votre critique / commentaire pour la famille") },
                placeholder = { Text("Qu'avez-vous particulièrement aimé ou moins aimé ?", color = CinemaTextSecondary) },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(14.dp),
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
                    .testTag("sheet_review_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    onSubmitReview(rating, comment)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sheet_submit_review_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PUBLIER L'AVIS (${activeMember?.name ?: "Moi"})",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
