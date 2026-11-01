package com.example.repository

import com.example.database.BudgetDao
import com.example.database.BudgetEntity
import com.example.database.DebtDao
import com.example.database.DebtEntity
import com.example.database.DompetKuDatabase
import com.example.database.SavingsGoalDao
import com.example.database.SavingsGoalEntity
import com.example.database.TransactionDao
import com.example.database.TransactionEntity
import com.example.database.WalletDao
import com.example.database.WalletEntity
import com.example.model.Budget
import com.example.model.Debt
import com.example.model.DebtType
import com.example.model.SavingsGoal
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.Wallet
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DompetKuRepository(
    private val database: DompetKuDatabase,
    private val transactionDao: TransactionDao = database.transactionDao(),
    private val walletDao: WalletDao = database.walletDao(),
    private val budgetDao: BudgetDao = database.budgetDao(),
    private val savingsGoalDao: SavingsGoalDao = database.savingsGoalDao(),
    private val debtDao: DebtDao = database.debtDao()
) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
        .map { list -> list.map { it.toDomainModel() } }

    val allWallets: Flow<List<Wallet>> = combine(
        walletDao.getAllWallets(),
        transactionDao.getAllTransactions()
    ) { walletEntities, transactionEntities ->
        val transactions = transactionEntities.map { it.toDomainModel() }

        walletEntities.map { walletEntity ->
            val walletId = walletEntity.id

            // Calculate current balance based on initial balance and all transactions
            var balance = walletEntity.initialBalance
            for (tx in transactions) {
                if (tx.walletId == walletId) {
                    when (tx.type) {
                        TransactionType.INCOME -> balance += tx.amount
                        TransactionType.EXPENSE -> balance -= tx.amount
                        TransactionType.TRANSFER -> balance -= tx.amount
                    }
                } else if (tx.toWalletId == walletId && tx.type == TransactionType.TRANSFER) {
                    balance += tx.amount
                }
            }

            walletEntity.toDomainModel(currentBalance = balance)
        }
    }

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> = combine(
        budgetDao.getBudgetsForMonth(month, year),
        transactionDao.getAllTransactions()
    ) { budgetEntities, transactionEntities ->
        val (monthStart, monthEnd) = DateUtils.getMonthRange(month, year)
        val monthTransactions = transactionEntities
            .map { it.toDomainModel() }
            .filter { it.timestamp in monthStart..monthEnd && it.type == TransactionType.EXPENSE }

        val spentByCategory = monthTransactions
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        budgetEntities.map { budgetEntity ->
            val spent = spentByCategory[budgetEntity.categoryId] ?: 0.0
            budgetEntity.toDomainModel(spentAmount = spent)
        }
    }

    val allSavingsGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
        .map { list -> list.map { it.toDomainModel() } }

    val allDebts: Flow<List<Debt>> = debtDao.getAllDebts()
        .map { list -> list.map { it.toDomainModel() } }

    suspend fun addTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction))
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun addWallet(wallet: Wallet) {
        walletDao.insertWallet(WalletEntity.fromDomain(wallet))
    }

    suspend fun updateWallet(wallet: Wallet) {
        walletDao.updateWallet(WalletEntity.fromDomain(wallet))
    }

    suspend fun deleteWallet(id: String) {
        walletDao.deleteWalletById(id)
    }

    suspend fun addBudget(budget: Budget): Long {
        return budgetDao.insertBudget(BudgetEntity.fromDomain(budget))
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(BudgetEntity.fromDomain(budget))
    }

    suspend fun deleteBudget(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): Long {
        return savingsGoalDao.insertGoal(SavingsGoalEntity.fromDomain(goal))
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(SavingsGoalEntity.fromDomain(goal))
    }

    suspend fun deleteSavingsGoal(id: Long) {
        savingsGoalDao.deleteGoalById(id)
    }

    suspend fun depositToSavingsGoal(goal: SavingsGoal, amount: Double, sourceWalletId: String) {
        val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
        savingsGoalDao.updateGoal(SavingsGoalEntity.fromDomain(updatedGoal))

        // Also record an expense transaction for the savings deposit
        val tx = Transaction(
            title = "Nabung: ${goal.title}",
            amount = amount,
            type = TransactionType.EXPENSE,
            categoryId = "bills",
            walletId = sourceWalletId,
            timestamp = System.currentTimeMillis(),
            notes = "Alokasi tabungan untuk target ${goal.title}"
        )
        transactionDao.insertTransaction(TransactionEntity.fromDomain(tx))
    }

    suspend fun addDebt(debt: Debt): Long {
        return debtDao.insertDebt(DebtEntity.fromDomain(debt))
    }

    suspend fun updateDebt(debt: Debt) {
        debtDao.updateDebt(DebtEntity.fromDomain(debt))
    }

    suspend fun deleteDebt(id: Long) {
        debtDao.deleteDebtById(id)
    }

    suspend fun depositToDebtSavings(
        debt: Debt,
        amount: Double,
        sourceWalletId: String?,
        recordWalletTransaction: Boolean
    ) {
        val updatedDebt = debt.copy(savedAmount = debt.savedAmount + amount)
        debtDao.updateDebt(DebtEntity.fromDomain(updatedDebt))

        if (recordWalletTransaction && sourceWalletId != null) {
            val txType = if (debt.type == DebtType.HUTANG) TransactionType.EXPENSE else TransactionType.INCOME
            val txTitle = if (debt.type == DebtType.HUTANG) "Nabung Cicilan: ${debt.title}" else "Terima Bayar Piutang: ${debt.title}"
            val tx = Transaction(
                title = txTitle,
                amount = amount,
                type = txType,
                categoryId = "bills",
                walletId = sourceWalletId,
                timestamp = System.currentTimeMillis(),
                notes = "Pencatatan dana pelunasan untuk ${debt.personName}"
            )
            transactionDao.insertTransaction(TransactionEntity.fromDomain(tx))
        }
    }

    suspend fun resetToDemoData() {
        DompetKuDatabase.populateInitialData(database)
    }
}
