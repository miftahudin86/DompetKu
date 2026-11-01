package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Budget
import com.example.model.Debt
import com.example.model.DebtType
import com.example.model.SavingsGoal
import com.example.ui.components.BudgetProgressItem
import com.example.ui.components.DebtItemCard
import com.example.ui.components.SavingsGoalItem
import com.example.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    budgets: List<Budget>,
    savingsGoals: List<SavingsGoal>,
    debts: List<Debt>,
    initialTab: Int = 0,
    onAddBudgetClick: () -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddGoalClick: () -> Unit,
    onDepositGoalClick: (SavingsGoal) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onAddDebtClick: () -> Unit,
    onDepositDebtClick: (Debt) -> Unit,
    onDeleteDebt: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Anggaran, 1: Tabungan, 2: Catatan Hutang

    val totalBudgetLimit = budgets.sumOf { it.monthlyLimit }
    val totalBudgetSpent = budgets.sumOf { it.spentAmount }
    val remainingBudget = totalBudgetLimit - totalBudgetSpent

    val totalGoalTarget = savingsGoals.sumOf { it.targetAmount }
    val totalGoalCurrent = savingsGoals.sumOf { it.currentAmount }

    val totalActiveHutang = debts.filter { it.type == DebtType.HUTANG && !it.isSettled }.sumOf { it.remainingAmount }
    val totalDebtSaved = debts.filter { it.type == DebtType.HUTANG }.sumOf { it.savedAmount }
    val totalReceivables = debts.filter { it.type == DebtType.PIUTANG && !it.isSettled }.sumOf { it.remainingAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budgets_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Anggaran, Tabungan & Hutang",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Tabs
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Anggaran (${budgets.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Tabungan (${savingsGoals.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Hutang (${debts.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Anggaran Kategori
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Batas Anggaran",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(totalBudgetLimit),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Sisa Anggaran Aman",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(remainingBudget.coerceAtLeast(0.0)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingBudget >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Budget Button
                item {
                    Button(
                        onClick = onAddBudgetClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_budget_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Tambah Anggaran Baru")
                    }
                }

                // Budget Items
                if (budgets.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Belum ada anggaran kategori",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Atur batas belanja per kategori agar keuangan tetap terkendali.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    items(budgets, key = { it.id }) { budget ->
                        BudgetProgressItem(
                            budget = budget,
                            onDelete = { onDeleteBudget(budget.id) }
                        )
                    }
                }
            }

            1 -> {
                // Tab 1: Savings Goals
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Terkumpul",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(totalGoalCurrent),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Dari Target Keseluruhan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(totalGoalTarget),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Goal Button
                item {
                    Button(
                        onClick = onAddGoalClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_savings_goal_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Buat Target Menabung Baru")
                    }
                }

                if (savingsGoals.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada target menabung",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Buat target impianmu seperti Dana Darurat, Liburan, atau Beli Barang.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(savingsGoals, key = { it.id }) { goal ->
                        SavingsGoalItem(
                            goal = goal,
                            onDepositClick = { onDepositGoalClick(goal) },
                            onDelete = { onDeleteGoal(goal.id) }
                        )
                    }
                }
            }

            2 -> {
                // Tab 2: Catatan Hutang & Tabungan Pelunasan
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Sisa Hutang Berjalan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(totalActiveHutang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Dana Tabungan Pelunasan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(totalDebtSaved),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            if (totalReceivables > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Piutang di Orang Lain: ${CurrencyUtils.formatRupiah(totalReceivables)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                        }
                    }
                }

                // Add Debt Button
                item {
                    Button(
                        onClick = onAddDebtClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_debt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Catat Hutang / Piutang Baru")
                    }
                }

                if (debts.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada catatan hutang",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Catat pinjaman atau cicilanmu dan tabung dana pelunasannya secara bertahap.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(debts, key = { it.id }) { debt ->
                        DebtItemCard(
                            debt = debt,
                            onDepositClick = { onDepositDebtClick(debt) },
                            onDelete = { onDeleteDebt(debt.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
