package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.Wallet
import com.example.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        DebtEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DompetKuDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtDao(): DebtDao

    companion object {
        @Volatile
        private var INSTANCE: DompetKuDatabase? = null

        fun getInstance(context: Context): DompetKuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DompetKuDatabase::class.java,
                    "dompetku_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: DompetKuDatabase) {
            // Wallets
            val walletEntities = Wallet.DEFAULT_WALLETS.map { WalletEntity.fromDomain(it) }
            database.walletDao().insertAll(walletEntities)

            // Current Month & Year
            val month = DateUtils.getCurrentMonth()
            val year = DateUtils.getCurrentYear()

            // Budgets for current month
            val initialBudgets = listOf(
                BudgetEntity(categoryId = "food", monthlyLimit = 2500000.0, month = month, year = year),
                BudgetEntity(categoryId = "transport", monthlyLimit = 800000.0, month = month, year = year),
                BudgetEntity(categoryId = "bills", monthlyLimit = 1500000.0, month = month, year = year),
                BudgetEntity(categoryId = "shopping", monthlyLimit = 2000000.0, month = month, year = year),
                BudgetEntity(categoryId = "entertainment", monthlyLimit = 600000.0, month = month, year = year)
            )
            database.budgetDao().insertAll(initialBudgets)

            // Savings Goals
            val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
            val initialGoals = listOf(
                SavingsGoalEntity(
                    title = "Dana Darurat (6 Bulan)",
                    targetAmount = 25000000.0,
                    currentAmount = 14500000.0,
                    targetDate = calendar.timeInMillis,
                    colorHex = 0xFF10B981,
                    iconName = "security",
                    notes = "Target 6x pengeluaran bulanan"
                ),
                SavingsGoalEntity(
                    title = "Liburan Akhir Tahun",
                    targetAmount = 8000000.0,
                    currentAmount = 4500000.0,
                    targetDate = calendar.apply { add(Calendar.MONTH, 2) }.timeInMillis,
                    colorHex = 0xFF3B82F6,
                    iconName = "flight",
                    notes = "Tiket & akomodasi"
                ),
                SavingsGoalEntity(
                    title = "Beli Laptop Kerja Baru",
                    targetAmount = 16000000.0,
                    currentAmount = 9800000.0,
                    targetDate = calendar.apply { add(Calendar.MONTH, 3) }.timeInMillis,
                    colorHex = 0xFFF59E0B,
                    iconName = "laptop",
                    notes = "Upgrade workstation"
                )
            )
            database.savingsGoalDao().insertAll(initialGoals)

            // Debts & Payoff Savings
            val debtCal1 = Calendar.getInstance().apply { add(Calendar.MONTH, 5) }
            val debtCal2 = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }
            val initialDebts = listOf(
                DebtEntity(
                    title = "Cicilan Motor Honda Vario",
                    personName = "FIF Finance",
                    type = "HUTANG",
                    totalAmount = 6000000.0,
                    savedAmount = 2500000.0,
                    dueDate = debtCal1.timeInMillis,
                    notes = "Tabungan cicilan sisa 5 bulan"
                ),
                DebtEntity(
                    title = "Pinjaman Modal Usaha",
                    personName = "Koperasi Sejahtera",
                    type = "HUTANG",
                    totalAmount = 4000000.0,
                    savedAmount = 1800000.0,
                    dueDate = debtCal2.timeInMillis,
                    notes = "Dicicil tiap tanggal 25"
                ),
                DebtEntity(
                    title = "Talangan Tiket Konser",
                    personName = "Rian (Teman)",
                    type = "PIUTANG",
                    totalAmount = 850000.0,
                    savedAmount = 0.0,
                    dueDate = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.timeInMillis,
                    notes = "Janji transfer akhir bulan"
                )
            )
            database.debtDao().insertAll(initialDebts)

            // Transactions
            val now = System.currentTimeMillis()
            val oneHour = 3600000L
            val oneDay = 86400000L

            val initialTransactions = listOf(
                TransactionEntity(
                    title = "Gaji Bulanan",
                    amount = 10000000.0,
                    type = "INCOME",
                    categoryId = "salary",
                    walletId = "w_bca",
                    timestamp = now - (oneDay * 2) - (oneHour * 4),
                    notes = "Transfer gaji bulan ini"
                ),
                TransactionEntity(
                    title = "Bonus Kinerja Proyek",
                    amount = 2500000.0,
                    type = "INCOME",
                    categoryId = "bonus",
                    walletId = "w_bca",
                    timestamp = now - (oneDay * 4),
                    notes = "Apresiasi project Q3"
                ),
                TransactionEntity(
                    title = "Makan Siang Soto Ayam & Es Teh",
                    amount = 35000.0,
                    type = "EXPENSE",
                    categoryId = "food",
                    walletId = "w_cash",
                    timestamp = now - (oneHour * 2),
                    notes = "Soto Ayam Lamongan"
                ),
                TransactionEntity(
                    title = "Kopi Susu & Croissant",
                    amount = 45000.0,
                    type = "EXPENSE",
                    categoryId = "food",
                    walletId = "w_gopay",
                    timestamp = now - (oneHour * 5),
                    notes = "Nongkrong sore"
                ),
                TransactionEntity(
                    title = "Belanja Bulanan Supermarket",
                    amount = 680000.0,
                    type = "EXPENSE",
                    categoryId = "shopping",
                    walletId = "w_bca",
                    timestamp = now - (oneDay * 1),
                    notes = "Kebutuhan pokok & sabun"
                ),
                TransactionEntity(
                    title = "Bensin Pertalite Motor",
                    amount = 50000.0,
                    type = "EXPENSE",
                    categoryId = "transport",
                    walletId = "w_cash",
                    timestamp = now - (oneDay * 1) - (oneHour * 3),
                    notes = "Isi full tank SPBU"
                ),
                TransactionEntity(
                    title = "Tagihan Listrik PLN",
                    amount = 275000.0,
                    type = "EXPENSE",
                    categoryId = "bills",
                    walletId = "w_gopay",
                    timestamp = now - (oneDay * 3),
                    notes = "Token listrik 200rb + admin"
                ),
                TransactionEntity(
                    title = "Langganan Internet & Wifi",
                    amount = 385000.0,
                    type = "EXPENSE",
                    categoryId = "bills",
                    walletId = "w_bca",
                    timestamp = now - (oneDay * 5),
                    notes = "Tagihan IndiHome bulanan"
                ),
                TransactionEntity(
                    title = "Nonton Bioskop XXI & Popcorn",
                    amount = 125000.0,
                    type = "EXPENSE",
                    categoryId = "entertainment",
                    walletId = "w_gopay",
                    timestamp = now - (oneDay * 6),
                    notes = "Tiket bioskop akhir pekan"
                )
            )
            database.transactionDao().insertAll(initialTransactions)
        }
    }
}
