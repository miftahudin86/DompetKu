package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Debt
import com.example.model.DebtType
import com.example.model.Wallet
import com.example.utils.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DepositDebtDialog(
    debt: Debt,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirmDeposit: (amount: Double, sourceWalletId: String?, recordWalletTransaction: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHutang = debt.type == DebtType.HUTANG
    var amountInput by remember { mutableStateOf("") }
    var selectedWallet by remember { mutableStateOf(wallets.firstOrNull() ?: Wallet.DEFAULT_WALLETS[0]) }
    var recordTransaction by remember { mutableStateOf(true) }

    val parsedAmount = CurrencyUtils.parseAmount(amountInput)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHutang) "Nabung Pelunasan Hutang" else "Catat Pembayaran Piutang",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = debt.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${if (isHutang) "Pemberi pinjaman:" else "Peminjam:"} ${debt.personName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Current Progress Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Dana Terkumpul",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatCompactRupiah(debt.savedAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sisa Hutang",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatCompactRupiah(debt.remainingAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        if (clean.length <= 11) amountInput = clean
                    },
                    label = { Text(if (isHutang) "Jumlah yang Ditabung / Dibayar" else "Jumlah Pembayaran Diterima") },
                    placeholder = { Text(CurrencyUtils.formatRupiah(parsedAmount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_debt_amount_input")
                )

                // Quick Chips
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100000, 250000, 500000, 1000000).forEach { quickAmount ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                val current = parsedAmount
                                amountInput = (current + quickAmount).toLong().toString()
                            }
                        ) {
                            Text(
                                text = "+${CurrencyUtils.formatCompactRupiah(quickAmount.toDouble()).replace("Rp ", "")}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Pay In Full Chip
                    if (debt.remainingAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.clickable {
                                amountInput = debt.remainingAmount.toLong().toString()
                            }
                        ) {
                            Text(
                                text = "Lunasi (${CurrencyUtils.formatCompactRupiah(debt.remainingAmount)})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet Selection
                Text(
                    text = if (isHutang) "Ambil Dana dari Dompet:" else "Simpan ke Dompet:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wallets.forEach { wallet ->
                        FilterChip(
                            selected = selectedWallet.id == wallet.id,
                            onClick = { selectedWallet = wallet },
                            label = { Text("${wallet.name} (${CurrencyUtils.formatCompactRupiah(wallet.currentBalance)})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox: Record transaction in wallet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { recordTransaction = !recordTransaction },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordTransaction,
                        onCheckedChange = { recordTransaction = it }
                    )
                    Text(
                        text = if (isHutang) "Potong saldo dompet sebagai pengeluaran cicilan" else "Tambah saldo dompet sebagai pemasukan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedAmount > 0) {
                        onConfirmDeposit(
                            parsedAmount,
                            if (recordTransaction) selectedWallet.id else null,
                            recordTransaction
                        )
                    }
                },
                enabled = parsedAmount > 0,
                modifier = Modifier.testTag("confirm_deposit_debt_button")
            ) {
                Text(if (isHutang) "Setor Tabungan Pelunasan" else "Simpan Pembayaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        modifier = modifier
    )
}
