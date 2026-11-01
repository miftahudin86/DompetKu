package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Debt
import com.example.model.DebtType
import com.example.services.SmartFinanceAdvisor
import com.example.utils.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DompetKu", appName)
  }

  @Test
  fun `currency formatting is correct`() {
    val formatted = CurrencyUtils.formatRupiah(150000.0)
    assertEquals("Rp 150.000", formatted)
  }

  @Test
  fun `smart auto-categorization works for keywords`() {
    val foodCategory = SmartFinanceAdvisor.suggestCategory("Makan siang bakso")
    assertNotNull(foodCategory)
    assertEquals("food", foodCategory?.id)

    val transportCategory = SmartFinanceAdvisor.suggestCategory("Bensin motor full tank")
    assertNotNull(transportCategory)
    assertEquals("transport", transportCategory?.id)
  }

  @Test
  fun `debt model calculates remaining amount and savings progress correctly`() {
    val debt = Debt(
      id = 1L,
      title = "Cicilan Motor",
      personName = "FIF",
      type = DebtType.HUTANG,
      totalAmount = 5000000.0,
      savedAmount = 2000000.0
    )

    assertEquals(3000000.0, debt.remainingAmount, 0.01)
    assertEquals(40f, debt.progressPercentage, 0.01f)
    assertFalse(debt.isSettled)

    // After saving the full remaining amount
    val fullySavedDebt = debt.copy(savedAmount = 5000000.0)
    assertEquals(0.0, fullySavedDebt.remainingAmount, 0.01)
    assertEquals(100f, fullySavedDebt.progressPercentage, 0.01f)
    assertTrue(fullySavedDebt.isSettled)
  }
}
