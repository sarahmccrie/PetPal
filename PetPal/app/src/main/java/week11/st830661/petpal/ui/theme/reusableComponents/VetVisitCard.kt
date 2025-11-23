package week11.st830661.petpal.ui.theme.reusableComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import week11.st830661.petpal.data.models.Appointment

@Composable
fun VetVisitCard(
    title: String,
    nextVisit: String,
    vetName: String = "",
    backgroundColor: Color,
    petImageUrl: String,
    appointment: Appointment? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (vetName.isNotEmpty()) {
                Text(
                    text = "Vet: $vetName",
                    fontSize = 12.sp,
                    color = Color(0xFF7D9C3C)
                )
            }
            Text(
                text = nextVisit,
                fontSize = 12.sp,
                color = Color(0xFF7D9C3C)
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            if (petImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = petImageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "🐾",
                    fontSize = 48.sp
                )
            }
        }
    }
}