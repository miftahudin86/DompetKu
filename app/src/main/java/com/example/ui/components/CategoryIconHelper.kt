package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName) {
            "restaurant" -> Icons.Default.Restaurant
            "directions_car" -> Icons.Default.DirectionsCar
            "shopping_bag" -> Icons.Default.ShoppingBag
            "receipt_long" -> Icons.Default.ReceiptLong
            "sports_esports" -> Icons.Default.SportsEsports
            "favorite" -> Icons.Default.Favorite
            "school" -> Icons.Default.School
            "home" -> Icons.Default.Home
            "payments" -> Icons.Default.Payments
            "work" -> Icons.Default.Work
            "trending_up" -> Icons.Default.TrendingUp
            "card_giftcard" -> Icons.Default.CardGiftcard
            "attach_money" -> Icons.Default.AttachMoney
            "swap_horiz" -> Icons.Default.SwapHoriz
            "account_balance" -> Icons.Default.AccountBalance
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "security" -> Icons.Default.Security
            "flight" -> Icons.Default.Flight
            "laptop" -> Icons.Default.Laptop
            "savings" -> Icons.Default.Savings
            "warning" -> Icons.Default.Warning
            "error" -> Icons.Default.Error
            "notifications_active" -> Icons.Default.NotificationsActive
            "check_circle" -> Icons.Default.CheckCircle
            "pie_chart" -> Icons.Default.PieChart
            "lightbulb" -> Icons.Default.Lightbulb
            else -> Icons.Default.Category
        }
    }
}
