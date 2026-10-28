package com.example.data.repository

import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.BudgetEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.Calendar

class ExpenseRepository(
  private val transactionDao: TransactionDao,
  private val budgetDao: BudgetDao,
  private val userProfileDao: UserProfileDao
) {

  fun getTransactions(userId: String): Flow<List<TransactionEntity>> =
    transactionDao.getAllTransactions(userId)

  fun getTransactionsInRange(userId: String, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> =
    transactionDao.getTransactionsInRange(userId, startMillis, endMillis)

  suspend fun insertTransaction(transaction: TransactionEntity): Long =
    transactionDao.insertTransaction(transaction)

  suspend fun updateTransaction(transaction: TransactionEntity) =
    transactionDao.updateTransaction(transaction)

  suspend fun deleteTransaction(transaction: TransactionEntity) =
    transactionDao.deleteTransaction(transaction)

  suspend fun deleteTransactionById(id: Long) =
    transactionDao.deleteTransactionById(id)

  fun getBudgets(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>> =
    budgetDao.getBudgetsForMonth(userId, month, year)

  fun getAllBudgets(userId: String): Flow<List<BudgetEntity>> =
    budgetDao.getAllBudgets(userId)

  suspend fun insertBudget(budget: BudgetEntity): Long =
    budgetDao.insertBudget(budget)

  suspend fun updateBudget(budget: BudgetEntity) =
    budgetDao.updateBudget(budget)

  suspend fun deleteBudget(budget: BudgetEntity) =
    budgetDao.deleteBudget(budget)

  suspend fun deleteBudgetById(id: Long) =
    budgetDao.deleteBudgetById(id)

  fun getUserProfile(userId: String): Flow<UserProfileEntity?> =
    userProfileDao.getUserProfile(userId)

  suspend fun getUserProfileOnce(userId: String): UserProfileEntity? =
    userProfileDao.getUserProfileOnce(userId)

  suspend fun updateUserProfile(user: UserProfileEntity) =
    userProfileDao.updateUser(user)

  suspend fun insertUserProfile(user: UserProfileEntity) =
    userProfileDao.insertUser(user)

  // Password / PIN hashing security utility
  fun hashPin(pin: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest("SALT_EXPENSE_APP_$pin".toByteArray())
    return digest.fold("") { str, it -> str + "%02x".format(it) }
  }

  suspend fun seedInitialDataIfEmpty(userId: String = "user_default") {
    val existingUser = userProfileDao.getUserProfileOnce(userId)
    if (existingUser == null) {
      userProfileDao.insertUser(
        UserProfileEntity(
          id = userId,
          username = "Aarav Sharma",
          email = "aarav.sharma@example.in",
          pinHash = "", // no PIN by default
          isPinLockEnabled = false,
          isBiometricEnabled = true,
          currencySymbol = "₹",
          monthlyIncomeTarget = 85000.0,
          monthlySavingsGoal = 25000.0
        )
      )
    }

    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH) + 1
    val currentYear = cal.get(Calendar.YEAR)
    val now = System.currentTimeMillis()
    val oneDay = 86_400_000L

    // Current month start
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 9)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfThisMonth = cal.timeInMillis

    val txCount = transactionDao.getCount(userId)

    // Build rich sample transactions guaranteed to fall inside the current month
    fun generateCurrentMonthTransactions(): List<TransactionEntity> {
      return listOf(
        TransactionEntity(
          userId = userId,
          title = "Infosys Tech Monthly Paycheck",
          amount = 85000.0,
          type = TransactionType.INCOME,
          category = "Salary",
          dateMillis = startOfThisMonth, // 1st of this month
          paymentMethod = PaymentMethod.BANK_TRANSFER,
          notes = "Direct salary credit in HDFC salary account"
        ),
        TransactionEntity(
          userId = userId,
          title = "Fintech Client App UI Consulting",
          amount = 22000.0,
          type = TransactionType.INCOME,
          category = "Freelance / Gig",
          dateMillis = now - (oneDay * 1).coerceAtLeast(0L),
          paymentMethod = PaymentMethod.UPI,
          notes = "Milestone payment via PhonePe"
        ),
        TransactionEntity(
          userId = userId,
          title = "TCS & Infosys Equity Dividend",
          amount = 4250.0,
          type = TransactionType.INCOME,
          category = "Dividend",
          dateMillis = startOfThisMonth + (oneDay * 2).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.BANK_TRANSFER,
          notes = "Quarterly dividend credited to bank"
        ),
        TransactionEntity(
          userId = userId,
          title = "3BHK Society Flat Rent & Maintenance",
          amount = 24000.0,
          type = TransactionType.EXPENSE,
          category = "Rent / Maintenance",
          dateMillis = startOfThisMonth + (oneDay * 1).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.NET_BANKING,
          notes = "Apartment monthly rent paid to landlord"
        ),
        TransactionEntity(
          userId = userId,
          title = "Zerodha Coin Nifty 50 Index Fund SIP",
          amount = 10000.0,
          type = TransactionType.EXPENSE,
          category = "SIP / Mutual Funds",
          dateMillis = startOfThisMonth + (oneDay * 3).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.UPI,
          notes = "Monthly wealth compounding auto-debit"
        ),
        TransactionEntity(
          userId = userId,
          title = "DMart Monthly Ration & Groceries",
          amount = 6850.0,
          type = TransactionType.EXPENSE,
          category = "Groceries / Ration",
          dateMillis = now - (oneDay * 2).coerceAtLeast(0L),
          paymentMethod = PaymentMethod.UPI,
          notes = "Monthly staples, rice, dal, oils, detergent"
        ),
        TransactionEntity(
          userId = userId,
          title = "Cook & Maid Monthly Salary",
          amount = 4500.0,
          type = TransactionType.EXPENSE,
          category = "Maid / Cook Salary",
          dateMillis = startOfThisMonth + (oneDay * 4).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.CASH,
          notes = "Housekeeping and cooking assistance"
        ),
        TransactionEntity(
          userId = userId,
          title = "Swiggy Gourmet Family Dinner & Biryani",
          amount = 1420.0,
          type = TransactionType.EXPENSE,
          category = "Swiggy / Zomato",
          dateMillis = now - (3600_000L * 4), // 4 hours ago
          paymentMethod = PaymentMethod.UPI,
          notes = "Weekend dinner order"
        ),
        TransactionEntity(
          userId = userId,
          title = "Tata Power Electricity Bill",
          amount = 2350.0,
          type = TransactionType.EXPENSE,
          category = "Utilities (Power/Gas/Water)",
          dateMillis = startOfThisMonth + (oneDay * 5).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.UPI,
          notes = "Monthly BESCOM/Tata Power electricity bill"
        ),
        TransactionEntity(
          userId = userId,
          title = "Jio Fiber & Mobile 5G 3-Month Plan",
          amount = 1499.0,
          type = TransactionType.EXPENSE,
          category = "Mobile / Broadband",
          dateMillis = now - (oneDay * 1).coerceAtLeast(0L),
          paymentMethod = PaymentMethod.UPI,
          notes = "High-speed broadband and 5G unlimited recharge"
        ),
        TransactionEntity(
          userId = userId,
          title = "Indian Oil Petrol Full Tank & Uber Cab",
          amount = 3200.0,
          type = TransactionType.EXPENSE,
          category = "Fuel / Cab (Ola/Uber)",
          dateMillis = now - (3600_000L * 18),
          paymentMethod = PaymentMethod.CREDIT_CARD,
          notes = "Petrol bunk refuel and airport Uber cab"
        ),
        TransactionEntity(
          userId = userId,
          title = "HDFC Ergo Health & LIC Term Insurance",
          amount = 2800.0,
          type = TransactionType.EXPENSE,
          category = "Insurance (LIC/Health)",
          dateMillis = startOfThisMonth + (oneDay * 2).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.NET_BANKING,
          notes = "Annual health insurance monthly installment"
        ),
        TransactionEntity(
          userId = userId,
          title = "Amazon Great Indian Festival Purchases",
          amount = 5499.0,
          type = TransactionType.EXPENSE,
          category = "Shopping",
          dateMillis = now - (oneDay * 3).coerceAtLeast(0L),
          paymentMethod = PaymentMethod.CREDIT_CARD,
          notes = "Home appliances and festive clothing"
        ),
        TransactionEntity(
          userId = userId,
          title = "Netflix & Disney+ Hotstar VIP",
          amount = 999.0,
          type = TransactionType.EXPENSE,
          category = "OTT Subscriptions",
          dateMillis = startOfThisMonth + (oneDay * 3).coerceAtMost(now - startOfThisMonth),
          paymentMethod = PaymentMethod.UPI,
          notes = "Monthly entertainment streaming plan"
        )
      )
    }

    if (txCount == 0) {
      transactionDao.insertAll(generateCurrentMonthTransactions())
    }

    val budgetCount = budgetDao.getCount(userId)
    if (budgetCount == 0) {
      val sampleBudgets = listOf(
        BudgetEntity(userId = userId, category = "Groceries / Ration", limitAmount = 12000.0, periodMonth = currentMonth, periodYear = currentYear),
        BudgetEntity(userId = userId, category = "Rent / Maintenance", limitAmount = 26000.0, periodMonth = currentMonth, periodYear = currentYear),
        BudgetEntity(userId = userId, category = "Swiggy / Zomato", limitAmount = 4500.0, periodMonth = currentMonth, periodYear = currentYear),
        BudgetEntity(userId = userId, category = "Fuel / Cab (Ola/Uber)", limitAmount = 5000.0, periodMonth = currentMonth, periodYear = currentYear),
        BudgetEntity(userId = userId, category = "Utilities (Power/Gas/Water)", limitAmount = 4000.0, periodMonth = currentMonth, periodYear = currentYear),
        BudgetEntity(userId = userId, category = "Shopping", limitAmount = 8000.0, periodMonth = currentMonth, periodYear = currentYear),
        BudgetEntity(userId = userId, category = "Maid / Cook Salary", limitAmount = 5000.0, periodMonth = currentMonth, periodYear = currentYear)
      )
      budgetDao.insertAll(sampleBudgets)
    }
  }
}
