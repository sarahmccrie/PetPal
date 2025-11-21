package week11.st830661.petpal.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

enum class NavItem(
    val icon: ImageVector?,
    val label: String,
    val emoji: String? = null
) {
    Dashboard(Icons.Default.Home, "Dashboard"),
    Pets(null, "Pets", "🐾"),
    Health(null, "Health", "❤️"),
    Reminders(Icons.Default.Notifications, "Reminders")
}

@Composable
fun BottomNavigationBar(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit
) {
    NavigationBar {
        NavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    if (item.emoji != null) {
                        Text(item.emoji, fontSize = 20.sp)
                    } else if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label, fontSize = 12.sp) }
            )
        }
    }
}
