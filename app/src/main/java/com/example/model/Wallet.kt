package com.example.model

enum class WalletType {
    CASH,       // Tunai
    BANK,       // Rekening Bank (BCA, Mandiri, BRI, BNI, Jago, dll)
    EWALLET,    // Dompet Digital (GoPay, OVO, ShopeePay, DANA)
    INVESTMENT, // Reksadana, Saham, Crypto
    OTHER
}

data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType,
    val initialBalance: Double,
    val currentBalance: Double = initialBalance,
    val colorHex: Long,
    val iconName: String,
    val accountNumber: String = ""
) {
    companion object {
        val DEFAULT_WALLETS = listOf(
            Wallet("w_cash", "Dompet Tunai", WalletType.CASH, 1250000.0, 1250000.0, 0xFF10B981, "payments"),
            Wallet("w_bca", "Bank BCA", WalletType.BANK, 8750000.0, 8750000.0, 0xFF00569B, "account_balance", "8801-4421-90"),
            Wallet("w_gopay", "GoPay", WalletType.EWALLET, 340000.0, 340000.0, 0xFF00AED6, "account_balance_wallet", "0812-3456-7890"),
            Wallet("w_mandiri", "Bank Mandiri", WalletType.BANK, 3200000.0, 3200000.0, 0xFFF59E0B, "account_balance", "137-00-1982-3")
        )
    }
}
