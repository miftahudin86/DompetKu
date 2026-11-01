package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Debt
import com.example.model.SavingsGoal
import com.example.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.components.AddBudgetBottomSheet
import com.example.ui.components.AddDebtBottomSheet
import com.example.ui.components.AddSavingsGoalBottomSheet
import com.example.ui.components.AddTransactionBottomSheet
import com.example.ui.components.AddWalletBottomSheet
import com.example.ui.components.DepositDebtDialog
import com.example.ui.components.DepositSavingsGoalDialog
import com.example.ui.components.FinancialAdvisorDialog
import kotlinx.coroutines.launch

enum class MainNavTab(val title: String) {
    DASHBOARD("Beranda"),
    TRANSACTIONS("Transaksi"),
    BUDGETS("Anggaran"),
    ANALYTICS("Statistik"),
    WALLETS("Dompet")
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(MainNavTab.DASHBOARD) }
    var budgetsInitialTab by remember { mutableIntStateOf(0) }

    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val monthlyIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val monthlyExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val allWallets by viewModel.allWallets.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val currentBudgets by viewModel.currentBudgets.collectAsStateWithLifecycle()
    val allSavingsGoals by viewModel.allSavingsGoals.collectAsStateWithLifecycle()
    val allDebts by viewModel.allDebts.collectAsStateWithLifecycle()
    val totalActiveDebt by viewModel.totalActiveDebt.collectAsStateWithLifecycle()
    val totalDebtSaved by viewModel.totalDebtSaved.collectAsStateWithLifecycle()
    val categoryExpenses by viewModel.categoryExpenses.collectAsStateWithLifecycle()
    val financialReport by viewModel.financialReport.collectAsStateWithLifecycle()

    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Sheet / Dialog States
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showAddBudgetSheet by remember { mutableStateOf(false) }
    var showAddGoalSheet by remember { mutableStateOf(false) }
    var showAddWalletSheet by remember { mutableStateOf(false) }
    var showAddDebtSheet by remember { mutableStateOf(false) }
    var goalForDeposit by remember { mutableStateOf<SavingsGoal?>(null) }
    var debtForDeposit by remember { mutableStateOf<Debt?>(null) }
    var showAdvisorDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle back button: if not on dashboard, return to dashboard
    BackHandler(enabled = currentTab != MainNavTab.DASHBOARD) {
        currentTab = MainNavTab.DASHBOARD
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                // Beranda
                NavigationBarItem(
                    selected = currentTab == MainNavTab.DASHBOARD,
                    onClick = { currentTab = MainNavTab.DASHBOARD },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainNavTab.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Beranda"
                        )
                    },
                    label = { Text("Beranda") },
                    modifier = Modifier.testTag("nav_dashboard")
                )

                // Transaksi
                NavigationBarItem(
                    selected = currentTab == MainNavTab.TRANSACTIONS,
                    onClick = { currentTab = MainNavTab.TRANSACTIONS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainNavTab.TRANSACTIONS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Transaksi"
                        )
                    },
                    label = { Text("Transaksi") },
                    modifier = Modifier.testTag("nav_transactions")
                )

                // Anggaran & Hutang
                NavigationBarItem(
                    selected = currentTab == MainNavTab.BUDGETS,
                    onClick = {
                        budgetsInitialTab = 0
                        currentTab = MainNavTab.BUDGETS
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainNavTab.BUDGETS) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Anggaran"
                        )
                    },
                    label = { Text("Anggaran") },
                    modifier = Modifier.testTag("nav_budgets")
                )

                // Statistik
                NavigationBarItem(
                    selected = currentTab == MainNavTab.ANALYTICS,
                    onClick = { currentTab = MainNavTab.ANALYTICS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainNavTab.ANALYTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Statistik"
                        )
                    },
                    label = { Text("Statistik") },
                    modifier = Modifier.testTag("nav_analytics")
                )

                // Dompet
                NavigationBarItem(
                    selected = currentTab == MainNavTab.WALLETS,
                    onClick = { currentTab = MainNavTab.WALLETS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainNavTab.WALLETS) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Dompet"
                        )
                    },
                    label = { Text("Dompet") },
                    modifier = Modifier.testTag("nav_wallets")
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTransactionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Catat Transaksi"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    MainNavTab.DASHBOARD -> DashboardScreen(
                        totalBalance = totalBalance,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        totalActiveDebt = totalActiveDebt,
                        totalDebtSaved = totalDebtSaved,
                        wallets = allWallets,
                        recentTransactions = filteredTransactions,
                        financialReport = financialReport,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onMonthYearSelected = { m, y -> viewModel.setMonthYear(m, y) },
                        onAddExpenseClick = { showAddTransactionSheet = true },
                        onAddIncomeClick = { showAddTransactionSheet = true },
                        onTransferClick = { showAddTransactionSheet = true },
                        onAddDebtClick = { showAddDebtSheet = true },
                        onOpenDebtScreen = {
                            budgetsInitialTab = 2
                            currentTab = MainNavTab.BUDGETS
                        },
                        onOpenAdvisor = { showAdvisorDialog = true },
                        onViewAllTransactions = { currentTab = MainNavTab.TRANSACTIONS },
                        onDeleteTransaction = { id ->
                            viewModel.deleteTransaction(id)
                            scope.launch { snackbarHostState.showSnackbar("Transaksi dihapus") }
                        },
                        onWalletClick = { currentTab = MainNavTab.WALLETS }
                    )

                    MainNavTab.TRANSACTIONS -> TransactionsScreen(
                        transactions = filteredTransactions,
                        wallets = allWallets,
                        selectedTypeFilter = selectedTypeFilter,
                        searchQuery = searchQuery,
                        onFilterChanged = { viewModel.setFilterType(it) },
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onDeleteTransaction = { id ->
                            viewModel.deleteTransaction(id)
                            scope.launch { snackbarHostState.showSnackbar("Transaksi dihapus") }
                        }
                    )

                    MainNavTab.BUDGETS -> BudgetsScreen(
                        budgets = currentBudgets,
                        savingsGoals = allSavingsGoals,
                        debts = allDebts,
                        initialTab = budgetsInitialTab,
                        onAddBudgetClick = { showAddBudgetSheet = true },
                        onDeleteBudget = { id ->
                            viewModel.deleteBudget(id)
                            scope.launch { snackbarHostState.showSnackbar("Anggaran dihapus") }
                        },
                        onAddGoalClick = { showAddGoalSheet = true },
                        onDepositGoalClick = { goal -> goalForDeposit = goal },
                        onDeleteGoal = { id ->
                            viewModel.deleteSavingsGoal(id)
                            scope.launch { snackbarHostState.showSnackbar("Target dihapus") }
                        },
                        onAddDebtClick = { showAddDebtSheet = true },
                        onDepositDebtClick = { debt -> debtForDeposit = debt },
                        onDeleteDebt = { id ->
                            viewModel.deleteDebt(id)
                            scope.launch { snackbarHostState.showSnackbar("Catatan hutang dihapus") }
                        }
                    )

                    MainNavTab.ANALYTICS -> AnalyticsScreen(
                        categoryExpenses = categoryExpenses,
                        totalExpense = monthlyExpense,
                        totalIncome = monthlyIncome,
                        financialReport = financialReport,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onMonthYearSelected = { m, y -> viewModel.setMonthYear(m, y) },
                        onOpenAdvisor = { showAdvisorDialog = true }
                    )

                    MainNavTab.WALLETS -> WalletsScreen(
                        wallets = allWallets,
                        totalBalance = totalBalance,
                        onAddWalletClick = { showAddWalletSheet = true },
                        onTransferClick = { showAddTransactionSheet = true },
                        onDeleteWallet = { id ->
                            viewModel.deleteWallet(id)
                            scope.launch { snackbarHostState.showSnackbar("Dompet dihapus") }
                        }
                    )
                }
            }
        }
    }

    // Sheets & Dialogs
    if (showAddTransactionSheet) {
        AddTransactionBottomSheet(
            wallets = allWallets,
            onDismiss = { showAddTransactionSheet = false },
            onSaveTransaction = { tx ->
                viewModel.addTransaction(tx)
                showAddTransactionSheet = false
                scope.launch {
                    val label = when (tx.type) {
                        TransactionType.EXPENSE -> "Pengeluaran berhasil dicatat!"
                        TransactionType.INCOME -> "Pemasukan berhasil dicatat!"
                        TransactionType.TRANSFER -> "Transfer saldo berhasil dicatat!"
                    }
                    snackbarHostState.showSnackbar(label)
                }
            }
        )
    }

    if (showAddBudgetSheet) {
        AddBudgetBottomSheet(
            onDismiss = { showAddBudgetSheet = false },
            onSaveBudget = { budget ->
                viewModel.addBudget(budget)
                showAddBudgetSheet = false
                scope.launch { snackbarHostState.showSnackbar("Anggaran baru berhasil disimpan!") }
            }
        )
    }

    if (showAddGoalSheet) {
        AddSavingsGoalBottomSheet(
            onDismiss = { showAddGoalSheet = false },
            onSaveGoal = { goal ->
                viewModel.addSavingsGoal(goal)
                showAddGoalSheet = false
                scope.launch { snackbarHostState.showSnackbar("Target menabung berhasil dibuat!") }
            }
        )
    }

    if (showAddDebtSheet) {
        AddDebtBottomSheet(
            onDismiss = { showAddDebtSheet = false },
            onSaveDebt = { debt ->
                viewModel.addDebt(debt)
                showAddDebtSheet = false
                scope.launch { snackbarHostState.showSnackbar("Catatan hutang '${debt.title}' berhasil disimpan!") }
            }
        )
    }

    if (showAddWalletSheet) {
        AddWalletBottomSheet(
            onDismiss = { showAddWalletSheet = false },
            onSaveWallet = { wallet ->
                viewModel.addWallet(wallet)
                showAddWalletSheet = false
                scope.launch { snackbarHostState.showSnackbar("Dompet ${wallet.name} berhasil ditambahkan!") }
            }
        )
    }

    goalForDeposit?.let { goal ->
        DepositSavingsGoalDialog(
            goal = goal,
            wallets = allWallets,
            onDismiss = { goalForDeposit = null },
            onConfirmDeposit = { amount, walletId ->
                viewModel.depositToSavingsGoal(goal, amount, walletId)
                goalForDeposit = null
                scope.launch { snackbarHostState.showSnackbar("Berhasil menabung untuk ${goal.title}!") }
            }
        )
    }

    debtForDeposit?.let { debt ->
        DepositDebtDialog(
            debt = debt,
            wallets = allWallets,
            onDismiss = { debtForDeposit = null },
            onConfirmDeposit = { amount, sourceWalletId, recordWalletTransaction ->
                viewModel.depositToDebtSavings(debt, amount, sourceWalletId, recordWalletTransaction)
                val isNowSettled = (debt.savedAmount + amount) >= debt.totalAmount
                debtForDeposit = null
                scope.launch {
                    if (isNowSettled) {
                        snackbarHostState.showSnackbar("Selamat! Hutang '${debt.title}' telah LUNAS terkumpul!")
                    } else {
                        snackbarHostState.showSnackbar("Berhasil menabung pelunasan untuk '${debt.title}'!")
                    }
                }
            }
        )
    }

    if (showAdvisorDialog) {
        FinancialAdvisorDialog(
            report = financialReport,
            onDismiss = { showAdvisorDialog = false }
        )
    }
}
