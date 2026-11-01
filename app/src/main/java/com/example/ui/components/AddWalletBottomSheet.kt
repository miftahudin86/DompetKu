package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Wallet
import com.example.model.WalletType
import com.example.utils.CurrencyUtils
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWalletBottomSheet(
    onDismiss: () -> Unit,
    onSaveWallet: (Wallet) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nameInput by remember { mutableStateOf("") }
    var initialBalanceInput by remember { mutableStateOf("") }
    var accountNumberInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.BANK) }

    val parsedBalance = CurrencyUtils.parseAmount(initialBalanceInput)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier.testTag("add_wallet_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tambah Dompet / Rekening",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jenis Akun",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    WalletType.BANK to "Rekening Bank",
                    WalletType.EWALLET to "E-Wallet",
                    WalletType.CASH to "Tunai",
                    WalletType.INVESTMENT to "Investasi"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Nama Dompet (cth: Bank Mandiri, GoPay, ShopeePay)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accountNumberInput,
                onValueChange = { accountNumberInput = it },
                label = { Text("Nomor Rekening / No. HP (opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = initialBalanceInput,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 12) initialBalanceInput = clean
                },
                label = { Text("Saldo Saat Ini") },
                placeholder = { Text(CurrencyUtils.formatRupiah(parsedBalance)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        val (icon, color) = when (selectedType) {
                            WalletType.CASH -> "payments" to 0xFF10B981
                            WalletType.BANK -> "account_balance" to 0xFF00569B
                            WalletType.EWALLET -> "account_balance_wallet" to 0xFF00AED6
                            WalletType.INVESTMENT -> "trending_up" to 0xFF047857
                            WalletType.OTHER -> "category" to 0xFF64748B
                        }
                        onSaveWallet(
                            Wallet(
                                id = "w_${UUID.randomUUID().toString().take(8)}",
                                name = nameInput.trim(),
                                type = selectedType,
                                initialBalance = parsedBalance,
                                currentBalance = parsedBalance,
                                colorHex = color,
                                iconName = icon,
                                accountNumber = accountNumberInput.trim()
                            )
                        )
                    }
                },
                enabled = nameInput.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_wallet_button")
            ) {
                Text("Simpan Dompet", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
