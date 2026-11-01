package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.DompetKuDatabase
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Debt
import com.example.model.DebtType
import com.example.model.SavingsGoal
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.Wallet
import com.example.repository.DompetKuRepository
import com.example.services.FinancialHealthReport
import com.example.services.SmartFinanceAdvisor
import com.example.ui.components.CategoryExpenseItem
import com.example.utils.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DompetKuRepository(DompetKuDatabase.getInstance(application))

    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(DateUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allWallets: StateFlow<List<Wallet>> = repository.allWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDebts: StateFlow<List<Debt>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Hutang (Remaining amount to pay)
    val totalActiveDebt: StateFlow<Double> = allDebts.map { debts ->
        debts.filter { it.type == DebtType.HUTANG && !it.isSettled }
            .sumOf { it.remainingAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total Tabungan Pelunasan Hutang yang sudah terkumpul
    val totalDebtSaved: StateFlow<Double> = allDebts.map { debts ->
        debts.filter { it.type == DebtType.HUTANG }
            .sumOf { it.savedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total Piutang yang masih belum dibayar ke user
    val totalReceivables: StateFlow<Double> = allDebts.map { debts ->
        debts.filter { it.type == DebtType.PIUTANG && !it.isSettled }
            .sumOf { it.remainingAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentBudgets: StateFlow<List<Budget>> = combine(_selectedMonth, _selectedYear) { month, year ->
        Pair(month, year)
    }.flatMapLatest { (month, year) ->
        repository.getBudgetsForMonth(month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for the selected month and search/type
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _selectedMonth,
        _selectedYear,
        _selectedTypeFilter,
        _searchQuery
    ) { txs, month, year, filterType, query ->
        val (monthStart, monthEnd) = DateUtils.getMonthRange(month, year)

        txs.filter { tx ->
            val inMonth = tx.timestamp in monthStart..monthEnd
            val matchesType = filterType == null || tx.type == filterType
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.notes.contains(query, ignoreCase = true) ||
                    Category.getById(tx.categoryId).name.contains(query, ignoreCase = true)

            inMonth && matchesType && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total Net Balance across all accounts
    val totalBalance: StateFlow<Double> = allWallets.combine(allTransactions) { wallets, _ ->
        wallets.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Current Month Income
    val monthlyIncome: StateFlow<Double> = combine(
        allTransactions,
        _selectedMonth,
        _selectedYear
    ) { txs, month, year ->
        val (start, end) = DateUtils.getMonthRange(month, year)
        txs.filter { it.timestamp in start..end && it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Current Month Expense
    val monthlyExpense: StateFlow<Double> = combine(
        allTransactions,
        _selectedMonth,
        _selectedYear
    ) { txs, month, year ->
        val (start, end) = DateUtils.getMonthRange(month, year)
        txs.filter { it.timestamp in start..end && it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category Breakdown Items for Donut Chart
    val categoryExpenses: StateFlow<List<CategoryExpenseItem>> = combine(
        allTransactions,
        _selectedMonth,
        _selectedYear
    ) { txs, month, year ->
        val (start, end) = DateUtils.getMonthRange(month, year)
        val monthExpenses = txs.filter { it.timestamp in start..end && it.type == TransactionType.EXPENSE }
        val total = monthExpenses.sumOf { it.amount }

        if (total <= 0) return@combine emptyList<CategoryExpenseItem>()

        val grouped = monthExpenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        grouped.map { (catId, amount) ->
            CategoryExpenseItem(
                category = Category.getById(catId),
                amount = amount,
                percentage = ((amount / total) * 100).toFloat()
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Health Report
    val financialReport: StateFlow<FinancialHealthReport> = combine(
        allTransactions,
        currentBudgets,
        _selectedMonth,
        _selectedYear
    ) { txs, budgets, month, year ->
        val (start, end) = DateUtils.getMonthRange(month, year)
        val monthTxs = txs.filter { it.timestamp in start..end }
        SmartFinanceAdvisor.analyzeFinancialHealth(monthTxs, budgets)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialHealthReport(
            score = 75,
            status = "Sehat & Terkendali",
            summary = "Pencatatan keuangan Anda berjalan dengan baik.",
            savingsRatePercentage = 25f,
            needsRatioPercentage = 45f,
            wantsRatioPercentage = 30f,
            keyInsights = emptyList()
        )
    )

    fun setFilterType(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.addWallet(wallet)
        }
    }

    fun deleteWallet(id: String) {
        viewModelScope.launch {
            repository.deleteWallet(id)
        }
    }

    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            repository.addBudget(budget)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.addSavingsGoal(goal)
        }
    }

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    fun depositToSavingsGoal(goal: SavingsGoal, amount: Double, sourceWalletId: String) {
        viewModelScope.launch {
            repository.depositToSavingsGoal(goal, amount, sourceWalletId)
        }
    }

    fun addDebt(debt: Debt) {
        viewModelScope.launch {
            repository.addDebt(debt)
        }
    }

    fun deleteDebt(id: Long) {
        viewModelScope.launch {
            repository.deleteDebt(id)
        }
    }

    fun depositToDebtSavings(debt: Debt, amount: Double, sourceWalletId: String?, recordTransaction: Boolean) {
        viewModelScope.launch {
            repository.depositToDebtSavings(debt, amount, sourceWalletId, recordTransaction)
        }
    }

    fun resetToDemo() {
        viewModelScope.launch {
            repository.resetToDemoData()
        }
    }
}
