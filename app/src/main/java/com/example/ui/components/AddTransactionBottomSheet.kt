package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.model.CategoryGroup
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.Wallet
import com.example.services.SmartFinanceAdvisor
import com.example.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionBottomSheet(
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onSaveTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Pengeluaran, 1: Pemasukan, 2: Transfer
    var amountInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val currentType = when (selectedTabIndex) {
        0 -> TransactionType.EXPENSE
        1 -> TransactionType.INCOME
        else -> TransactionType.TRANSFER
    }

    val availableCategories = remember(currentType) {
        when (currentType) {
            TransactionType.EXPENSE -> Category.DEFAULT_CATEGORIES.filter { it.group == CategoryGroup.EXPENSE }
            TransactionType.INCOME -> Category.DEFAULT_CATEGORIES.filter { it.group == CategoryGroup.INCOME }
            TransactionType.TRANSFER -> Category.DEFAULT_CATEGORIES.filter { it.group == CategoryGroup.TRANSFER }
        }
    }

    var selectedCategory by remember(currentType) {
        mutableStateOf(availableCategories.firstOrNull() ?: Category.DEFAULT_CATEGORIES[0])
    }

    var selectedSourceWallet by remember {
        mutableStateOf(wallets.firstOrNull() ?: Wallet.DEFAULT_WALLETS[0])
    }
    var selectedTargetWallet by remember {
        mutableStateOf(wallets.getOrNull(1) ?: wallets.firstOrNull() ?: Wallet.DEFAULT_WALLETS[0])
    }

    // Smart auto category suggestion
    val suggestedCategory = remember(titleInput) {
        if (titleInput.length >= 3 && currentType != TransactionType.TRANSFER) {
            SmartFinanceAdvisor.suggestCategory(titleInput)
        } else null
    }

    val parsedAmount = CurrencyUtils.parseAmount(amountInput)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier
            .testTag("add_transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catat Transaksi Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_transaction")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Pengeluaran, Pemasukan, Transfer
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Pengeluaran",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Pemasukan",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 1) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            "Transfer",
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 2) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nominal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(parsedAmount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (currentType) {
                            TransactionType.INCOME -> Color(0xFF10B981)
                            TransactionType.EXPENSE -> Color(0xFFEF4444)
                            TransactionType.TRANSFER -> Color(0xFF3B82F6)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 11) {
                                amountInput = clean
                            }
                        },
                        placeholder = { Text("Ketik jumlah (cth: 50000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input")
                    )

                    // Quick Chips (+10k, +50k, +100k, +500k)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(10000, 50000, 100000, 500000).forEach { quickAmount ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    val current = parsedAmount
                                    amountInput = (current + quickAmount).toLong().toString()
                                }
                            ) {
                                Text(
                                    text = "+${CurrencyUtils.formatCompactRupiah(quickAmount.toDouble()).replace("Rp ", "")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title / Description
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = {
                    Text(
                        when (currentType) {
                            TransactionType.TRANSFER -> "Keterangan Transfer (opsional)"
                            TransactionType.INCOME -> "Sumber Pemasukan (cth: Gaji, Jual Kue)"
                            TransactionType.EXPENSE -> "Keperluan (cth: Makan Siang Bakso, Bensin)"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input")
            )

            // Smart Auto-Categorization Chip
            AnimatedVisibility(visible = suggestedCategory != null && suggestedCategory.id != selectedCategory.id) {
                if (suggestedCategory != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD1FAE5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable {
                                selectedCategory = suggestedCategory
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saran Otomatis: ${suggestedCategory.name} (Ketuk untuk pilih)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection (For Income & Expense)
            if (currentType != TransactionType.TRANSFER) {
                Text(
                    text = "Pilih Kategori",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory.id == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(category.iconName),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(category.colorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("category_chip_${category.id}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Wallet Selection
            if (currentType == TransactionType.TRANSFER) {
                // Source and Destination Wallets
                Text(
                    text = "Dari Dompet (Sumber)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.forEach { wallet ->
                        val isSelected = selectedSourceWallet.id == wallet.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSourceWallet = wallet },
                            label = { Text(wallet.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ke Dompet (Tujuan)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.forEach { wallet ->
                        val isSelected = selectedTargetWallet.id == wallet.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTargetWallet = wallet },
                            label = { Text(wallet.name) }
                        )
                    }
                }
            } else {
                Text(
                    text = "Pilih Dompet / Rekening",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.forEach { wallet ->
                        val isSelected = selectedSourceWallet.id == wallet.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSourceWallet = wallet },
                            label = { Text("${wallet.name} (${CurrencyUtils.formatCompactRupiah(wallet.currentBalance)})") },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(wallet.iconName),
                                    contentDescription = null,
                                    tint = Color(wallet.colorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Input
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Catatan Tambahan (opsional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (parsedAmount > 0) {
                        val title = if (titleInput.isNotBlank()) titleInput.trim()
                        else when (currentType) {
                            TransactionType.TRANSFER -> "Transfer ${selectedSourceWallet.name} → ${selectedTargetWallet.name}"
                            else -> selectedCategory.name
                        }

                        val tx = Transaction(
                            title = title,
                            amount = parsedAmount,
                            type = currentType,
                            categoryId = if (currentType == TransactionType.TRANSFER) "transfer" else selectedCategory.id,
                            walletId = selectedSourceWallet.id,
                            toWalletId = if (currentType == TransactionType.TRANSFER) selectedTargetWallet.id else null,
                            timestamp = System.currentTimeMillis(),
                            notes = notesInput.trim()
                        )
                        onSaveTransaction(tx)
                    }
                },
                enabled = parsedAmount > 0 && (currentType != TransactionType.TRANSFER || selectedSourceWallet.id != selectedTargetWallet.id),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (currentType) {
                        TransactionType.INCOME -> Color(0xFF10B981)
                        TransactionType.EXPENSE -> Color(0xFFEF4444)
                        TransactionType.TRANSFER -> Color(0xFF3B82F6)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button")
            ) {
                Text(
                    text = "Simpan Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
