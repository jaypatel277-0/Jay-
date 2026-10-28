package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.BiometricAuthManager
import com.example.auth.BiometricAuthResult
import com.example.data.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserProfileEntity
import com.example.data.repository.ExpenseRepository
import com.example.ui.components.CategoryConstants
import com.example.ui.components.CategorySlice
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.TrendPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateRangeFilter(val displayName: String) {
  THIS_MONTH("This Month"),
  LAST_MONTH("Last Month"),
  THIS_YEAR("This Year"),
  ALL_TIME("All Time")
}

enum class SortOption(val displayName: String) {
  DATE_DESC("Newest First"),
  DATE_ASC("Oldest First"),
  AMOUNT_DESC("Highest Amount"),
  AMOUNT_ASC("Lowest Amount")
}

data class FinancialSummary(
  val totalIncome: Double = 0.0,
  val totalExpense: Double = 0.0,
  val netBalance: Double = 0.0,
  val savingsRate: Double = 0.0,
  val currencySymbol: String = "₹"
)

data class BudgetProgress(
  val budget: BudgetEntity,
  val spentAmount: Double,
  val percentUtilized: Float,
  val isExceeded: Boolean,
  val isWarning: Boolean
)

data class MonthOverMonthStats(
  val thisMonthExpense: Double = 0.0,
  val lastMonthExpense: Double = 0.0,
  val expenseChangePercent: Double = 0.0,
  val thisMonthIncome: Double = 0.0,
  val lastMonthIncome: Double = 0.0,
  val incomeChangePercent: Double = 0.0,
  val dailyAverageExpense: Double = 0.0
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ExpenseRepository
  val biometricAuthManager: BiometricAuthManager = BiometricAuthManager(application)
  val activeUserId = "user_default"

  // Filter & Search States
  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  private val _typeFilter = MutableStateFlow<TransactionType?>(null)
  val typeFilter = _typeFilter.asStateFlow()

  private val _categoryFilter = MutableStateFlow<String?>(null)
  val categoryFilter = _categoryFilter.asStateFlow()

  private val _paymentMethodFilter = MutableStateFlow<PaymentMethod?>(null)
  val paymentMethodFilter = _paymentMethodFilter.asStateFlow()

  private val _dateRangeFilter = MutableStateFlow(DateRangeFilter.THIS_MONTH)
  val dateRangeFilter = _dateRangeFilter.asStateFlow()

  private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
  val sortOption = _sortOption.asStateFlow()

  // Security / PIN / Biometric lock state
  private val _isAppLocked = MutableStateFlow(false)
  val isAppLocked = _isAppLocked.asStateFlow()

  private val _biometricStatusMessage = MutableStateFlow<String?>(null)
  val biometricStatusMessage = _biometricStatusMessage.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = ExpenseRepository(
      db.transactionDao(),
      db.budgetDao(),
      db.userProfileDao()
    )
    kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
      repository.seedInitialDataIfEmpty(activeUserId)
    }
    viewModelScope.launch {
      val user = repository.getUserProfileOnce(activeUserId)
      if (user != null && user.isPinLockEnabled && user.pinHash.isNotBlank()) {
        _isAppLocked.value = true
      }
    }
  }

  val userProfile: StateFlow<UserProfileEntity?> = repository.getUserProfile(activeUserId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allTransactions: StateFlow<List<TransactionEntity>> = repository.getTransactions(activeUserId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allBudgets: StateFlow<List<BudgetEntity>> = repository.getAllBudgets(activeUserId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered and Sorted Transactions
  val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
    allTransactions,
    _searchQuery,
    _typeFilter,
    _categoryFilter,
    _paymentMethodFilter,
    _dateRangeFilter,
    _sortOption
  ) { args ->
    val transactions = args[0] as List<TransactionEntity>
    val query = args[1] as String
    val type = args[2] as TransactionType?
    val category = args[3] as String?
    val method = args[4] as PaymentMethod?
    val dateRange = args[5] as DateRangeFilter
    val sort = args[6] as SortOption

    val cal = Calendar.getInstance()

    val (startRangeMillis, endRangeMillis) = when (dateRange) {
      DateRangeFilter.THIS_MONTH -> {
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        start to end
      }
      DateRangeFilter.LAST_MONTH -> {
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, -1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        start to end
      }
      DateRangeFilter.THIS_YEAR -> {
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.YEAR, 1)
        val end = cal.timeInMillis - 1
        start to end
      }
      DateRangeFilter.ALL_TIME -> 0L to Long.MAX_VALUE
    }

    var result = transactions.filter { tx ->
      val matchesQuery = query.isBlank() ||
        tx.title.contains(query, ignoreCase = true) ||
        tx.notes.contains(query, ignoreCase = true) ||
        tx.category.contains(query, ignoreCase = true)

      val matchesType = type == null || tx.type == type
      val matchesCategory = category == null || tx.category.equals(category, ignoreCase = true)
      val matchesMethod = method == null || tx.paymentMethod == method
      val matchesDate = tx.dateMillis in startRangeMillis..endRangeMillis

      matchesQuery && matchesType && matchesCategory && matchesMethod && matchesDate
    }

    result = when (sort) {
      SortOption.DATE_DESC -> result.sortedByDescending { it.dateMillis }
      SortOption.DATE_ASC -> result.sortedBy { it.dateMillis }
      SortOption.AMOUNT_DESC -> result.sortedByDescending { it.amount }
      SortOption.AMOUNT_ASC -> result.sortedBy { it.amount }
    }

    result
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Financial summary for the currently selected date range
  val financialSummary: StateFlow<FinancialSummary> = combine(
    filteredTransactions,
    userProfile
  ) { transactions, profile ->
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val net = income - expense
    val savingsRate = if (income > 0) {
      ((income - expense) / income * 100.0).coerceIn(-100.0, 100.0)
    } else 0.0

    FinancialSummary(
      totalIncome = income,
      totalExpense = expense,
      netBalance = net,
      savingsRate = savingsRate,
      currencySymbol = profile?.currencySymbol ?: "₹"
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

  // Category Breakdown Slices
  val categorySlices: StateFlow<List<CategorySlice>> = filteredTransactions.combine(userProfile) { txs, _ ->
    val expenses = txs.filter { it.type == TransactionType.EXPENSE }
    val totalExpense = expenses.sumOf { it.amount }
    if (totalExpense <= 0) return@combine emptyList()

    val grouped = expenses.groupBy { it.category }
    grouped.map { (cat, list) ->
      val sum = list.sumOf { it.amount }
      val pct = ((sum / totalExpense) * 100).toFloat()
      val meta = CategoryConstants.getCategoryMeta(cat)
      CategorySlice(
        category = cat,
        amount = sum,
        percentage = pct,
        color = meta.color
      )
    }.sortedByDescending { it.amount }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Spending Trends Points (Daily or Grouped)
  val spendingTrendPoints: StateFlow<List<TrendPoint>> = filteredTransactions.combine(userProfile) { txs, _ ->
    val expenses = txs.filter { it.type == TransactionType.EXPENSE }.sortedBy { it.dateMillis }
    if (expenses.isEmpty()) return@combine emptyList()

    val dayFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
    val grouped = expenses.groupBy { dayFormat.format(Date(it.dateMillis)) }
    grouped.map { (label, list) ->
      TrendPoint(
        label = label,
        amount = list.sumOf { it.amount }
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Budget Progress list for current month
  val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
    allBudgets,
    allTransactions
  ) { budgets, transactions ->
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val startOfMonth = cal.timeInMillis

    val thisMonthExpenses = transactions.filter {
      it.type == TransactionType.EXPENSE && it.dateMillis >= startOfMonth
    }

    budgets.map { budget ->
      val spent = thisMonthExpenses
        .filter { it.category.equals(budget.category, ignoreCase = true) }
        .sumOf { it.amount }
      val pct = if (budget.limitAmount > 0) ((spent / budget.limitAmount) * 100).toFloat() else 0f
      BudgetProgress(
        budget = budget,
        spentAmount = spent,
        percentUtilized = pct,
        isExceeded = pct >= 100f,
        isWarning = pct in 75f..99.9f
      )
    }.sortedByDescending { it.percentUtilized }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Month-Over-Month Comparison Stats
  val monthOverMonthStats: StateFlow<MonthOverMonthStats> = allTransactions.combine(userProfile) { txs, _ ->
    val cal = Calendar.getInstance()
    val currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)

    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val thisMonthStart = cal.timeInMillis

    cal.add(Calendar.MONTH, -1)
    val lastMonthStart = cal.timeInMillis
    val lastMonthEnd = thisMonthStart - 1

    val thisMonthTxs = txs.filter { it.dateMillis >= thisMonthStart }
    val lastMonthTxs = txs.filter { it.dateMillis in lastMonthStart..lastMonthEnd }

    val thisMonthExpense = thisMonthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val lastMonthExpense = lastMonthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val thisMonthIncome = thisMonthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val lastMonthIncome = lastMonthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val expenseChange = if (lastMonthExpense > 0) {
      ((thisMonthExpense - lastMonthExpense) / lastMonthExpense) * 100.0
    } else 0.0

    val incomeChange = if (lastMonthIncome > 0) {
      ((thisMonthIncome - lastMonthIncome) / lastMonthIncome) * 100.0
    } else 0.0

    val dailyAverage = thisMonthExpense / currentDayOfMonth

    MonthOverMonthStats(
      thisMonthExpense = thisMonthExpense,
      lastMonthExpense = lastMonthExpense,
      expenseChangePercent = expenseChange,
      thisMonthIncome = thisMonthIncome,
      lastMonthIncome = lastMonthIncome,
      incomeChangePercent = incomeChange,
      dailyAverageExpense = dailyAverage
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthOverMonthStats())

  // Transaction CRUD Operations
  fun addTransaction(transaction: TransactionEntity) {
    viewModelScope.launch {
      repository.insertTransaction(transaction.copy(userId = activeUserId))
    }
  }

  fun updateTransaction(transaction: TransactionEntity) {
    viewModelScope.launch {
      repository.updateTransaction(transaction.copy(userId = activeUserId))
    }
  }

  fun deleteTransaction(transaction: TransactionEntity) {
    viewModelScope.launch {
      repository.deleteTransaction(transaction)
    }
  }

  fun deleteTransactionById(id: Long) {
    viewModelScope.launch {
      repository.deleteTransactionById(id)
    }
  }

  // Budget CRUD Operations
  fun saveBudget(budget: BudgetEntity) {
    viewModelScope.launch {
      if (budget.id == 0L) {
        repository.insertBudget(budget.copy(userId = activeUserId))
      } else {
        repository.updateBudget(budget.copy(userId = activeUserId))
      }
    }
  }

  fun deleteBudget(budget: BudgetEntity) {
    viewModelScope.launch {
      repository.deleteBudget(budget)
    }
  }

  // Filter setters
  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setTypeFilter(type: TransactionType?) {
    _typeFilter.value = type
  }

  fun setCategoryFilter(category: String?) {
    _categoryFilter.value = category
  }

  fun setPaymentMethodFilter(method: PaymentMethod?) {
    _paymentMethodFilter.value = method
  }

  fun setDateRangeFilter(filter: DateRangeFilter) {
    _dateRangeFilter.value = filter
  }

  fun setSortOption(sort: SortOption) {
    _sortOption.value = sort
  }

  fun clearAllFilters() {
    _searchQuery.value = ""
    _typeFilter.value = null
    _categoryFilter.value = null
    _paymentMethodFilter.value = null
    _dateRangeFilter.value = DateRangeFilter.THIS_MONTH
    _sortOption.value = SortOption.DATE_DESC
  }

  // User & Biometric / PIN Methods
  fun unlockWithPin(enteredPin: String): Boolean {
    val currentProfile = userProfile.value ?: return true
    if (!currentProfile.isPinLockEnabled || currentProfile.pinHash.isBlank()) {
      _isAppLocked.value = false
      return true
    }
    val hash = repository.hashPin(enteredPin)
    val success = hash == currentProfile.pinHash
    if (success) {
      _isAppLocked.value = false
      _biometricStatusMessage.value = null
    }
    return success
  }

  fun authenticateWithBiometrics(activity: Activity) {
    viewModelScope.launch {
      when (val result = biometricAuthManager.authenticateWithCredentials(activity)) {
        is BiometricAuthResult.Success,
        is BiometricAuthResult.DeviceVerified -> {
          _isAppLocked.value = false
          _biometricStatusMessage.value = "Biometric authenticated successfully"
        }
        is BiometricAuthResult.Cancelled -> {
          _biometricStatusMessage.value = "Biometric cancelled. Enter PIN."
        }
        is BiometricAuthResult.Error -> {
          _biometricStatusMessage.value = result.message
        }
      }
    }
  }

  fun lockApp() {
    if (userProfile.value?.isPinLockEnabled == true) {
      _isAppLocked.value = true
    }
  }

  fun saveUserProfileSettings(
    username: String,
    email: String,
    currencySymbol: String,
    isBiometricEnabled: Boolean,
    isPinLockEnabled: Boolean,
    isDarkModeEnabled: Boolean,
    newPin: String?
  ) {
    viewModelScope.launch {
      val current = userProfile.value ?: repository.getUserProfileOnce(activeUserId) ?: UserProfileEntity(id = activeUserId)
      val pinHash = if (isPinLockEnabled && !newPin.isNullOrBlank()) {
        repository.hashPin(newPin)
      } else if (isPinLockEnabled) {
        current.pinHash
      } else {
        ""
      }

      val updated = current.copy(
        username = username.trim().ifBlank { "User" },
        email = email.trim().ifBlank { "user@example.in" },
        currencySymbol = currencySymbol.trim().ifBlank { "₹" },
        isBiometricEnabled = isBiometricEnabled,
        isPinLockEnabled = isPinLockEnabled,
        isDarkModeEnabled = isDarkModeEnabled,
        pinHash = pinHash
      )

      repository.insertUserProfile(updated)
      if (!isPinLockEnabled) {
        _isAppLocked.value = false
      }
    }
  }

  fun updatePinSettings(newPin: String?, isEnabled: Boolean) {
    viewModelScope.launch {
      val current = userProfile.value ?: repository.getUserProfileOnce(activeUserId) ?: UserProfileEntity(id = activeUserId)
      val newHash = if (!newPin.isNullOrBlank()) repository.hashPin(newPin) else current.pinHash
      val updated = current.copy(
        pinHash = newHash,
        isPinLockEnabled = isEnabled
      )
      repository.insertUserProfile(updated)
      if (!isEnabled) {
        _isAppLocked.value = false
      }
    }
  }

  fun updateProfile(username: String, email: String, currencySymbol: String, isBiometricEnabled: Boolean = true) {
    viewModelScope.launch {
      val current = userProfile.value ?: repository.getUserProfileOnce(activeUserId) ?: UserProfileEntity(id = activeUserId)
      val updated = current.copy(
        username = username.trim(),
        email = email.trim(),
        currencySymbol = currencySymbol.trim(),
        isBiometricEnabled = isBiometricEnabled
      )
      repository.insertUserProfile(updated)
    }
  }

  // Generate CSV string representation formatted for Indian financial market
  fun generateCsvContent(): String {
    val txs = allTransactions.value
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
    val curr = userProfile.value?.currencySymbol ?: "₹"
    val sb = StringBuilder()
    sb.append("ID,Date,Title,Type,Category,Amount ($curr),Formatted Amount,Payment Method,Notes\n")
    txs.forEach { tx ->
      val formattedDate = sdf.format(Date(tx.dateMillis))
      val escapedTitle = "\"${tx.title.replace("\"", "\"\"")}\""
      val escapedNotes = "\"${tx.notes.replace("\"", "\"\"")}\""
      val formattedIndian = "\"${IndianCurrencyFormatter.formatInr(tx.amount, includeSymbol = true, currencySymbol = curr)}\""
      sb.append("${tx.id},$formattedDate,$escapedTitle,${tx.type.name},${tx.category},${String.format(Locale.US, "%.2f", tx.amount)},$formattedIndian,${tx.paymentMethod.displayName},$escapedNotes\n")
    }
    return sb.toString()
  }

  fun exportCsvAndShare(context: android.content.Context) {
    try {
      val csvContent = generateCsvContent()
      val file = java.io.File(context.cacheDir, "expense_tracker_export.csv")
      file.writeText(csvContent)
      val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Expense Tracker CSV Report (INR)")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = android.content.Intent.createChooser(intent, "Export Transactions CSV")
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
