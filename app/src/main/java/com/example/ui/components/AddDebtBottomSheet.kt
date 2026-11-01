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
import com.example.model.Debt
import com.example.model.DebtType
import com.example.utils.CurrencyUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddDebtBottomSheet(
    onDismiss: () -> Unit,
    onSaveDebt: (Debt) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Hutang Saya, 1: Piutang
    val currentType = if (selectedTabIndex == 0) DebtType.HUTANG else DebtType.PIUTANG

    var titleInput by remember { mutableStateOf("") }
    var personNameInput by remember { mutableStateOf("") }
    var totalAmountInput by remember { mutableStateOf("") }
    var initialSavedInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var selectedDueMonths by remember { mutableIntStateOf(6) }

    val parsedTotal = CurrencyUtils.parseAmount(totalAmountInput)
    val parsedSaved = CurrencyUtils.parseAmount(initialSavedInput)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier.testTag("add_debt_bottom_sheet")
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
                    text = "Catat Hutang / Piutang",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Hutang Saya vs Piutang
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Hutang Saya",
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
                            "Piutang (Uang di Orang)",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 1) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Amount Input
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
                        text = if (currentType == DebtType.HUTANG) "Total Nominal Hutang" else "Total Piutang",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(parsedTotal),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (currentType == DebtType.HUTANG) Color(0xFFEF4444) else Color(0xFF3B82F6)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = totalAmountInput,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 11) totalAmountInput = clean
                        },
                        placeholder = { Text("Ketik nominal (cth: 5000000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_total_amount_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title / Keperluan
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = {
                    Text(if (currentType == DebtType.HUTANG) "Nama Hutang / Keperluan (cth: Cicilan Motor, KPR)" else "Keperluan Piutang (cth: Talangan Tiket)")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nama Pihak
            OutlinedTextField(
                value = personNameInput,
                onValueChange = { personNameInput = it },
                label = {
                    Text(if (currentType == DebtType.HUTANG) "Nama Pemberi Pinjaman / Lembaga (cth: Bank BCA, Ahmad)" else "Nama Peminjam / Teman (cth: Budi)")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_person_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Saldo Awal yang Sudah Ditabung
            OutlinedTextField(
                value = initialSavedInput,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 11) initialSavedInput = clean
                },
                label = {
                    Text(if (currentType == DebtType.HUTANG) "Tabungan Pelunasan Awal (opsional)" else "Sudah Terbayar Awal (opsional)")
                },
                placeholder = { Text(CurrencyUtils.formatRupiah(parsedSaved)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date / Jatuh Tempo
            Text(
                text = "Perkiraan Jatuh Tempo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 3, 6, 12, 24).forEach { months ->
                    FilterChip(
                        selected = selectedDueMonths == months,
                        onClick = { selectedDueMonths = months },
                        label = { Text("$months Bulan") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Catatan Tambahan (opsional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (titleInput.isNotBlank() && personNameInput.isNotBlank() && parsedTotal > 0) {
                        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, selectedDueMonths) }
                        onSaveDebt(
                            Debt(
                                title = titleInput.trim(),
                                personName = personNameInput.trim(),
                                type = currentType,
                                totalAmount = parsedTotal,
                                savedAmount = parsedSaved,
                                dueDate = calendar.timeInMillis,
                                notes = notesInput.trim()
                            )
                        )
                    }
                },
                enabled = titleInput.isNotBlank() && personNameInput.isNotBlank() && parsedTotal > 0,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentType == DebtType.HUTANG) Color(0xFF0F766E) else Color(0xFF3B82F6)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_debt_button")
            ) {
                Text(
                    text = "Simpan Catatan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
