package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthLockScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProfileSettingsDialog
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

enum class Screen(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val tag: String
) {
  DASHBOARD("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
  TRANSACTIONS("Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_transactions"),
  BUDGETS("Budgets", Icons.Filled.Savings, Icons.Outlined.Savings, "nav_budgets"),
  ANALYTICS("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_analytics")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: ExpenseViewModel = viewModel()
      val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
      val darkTheme = userProfile?.isDarkModeEnabled ?: androidx.compose.foundation.isSystemInDarkTheme()

      MyApplicationTheme(darkTheme = darkTheme) {
        ExpenseTrackerApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun ExpenseTrackerApp(
  viewModel: ExpenseViewModel = viewModel()
) {
  val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

  var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

  // Dialog States
  var showAddTransactionDialog by remember { mutableStateOf(false) }
  var defaultAddType by remember { mutableStateOf(TransactionType.EXPENSE) }
  var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
  var showProfileDialog by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Handle system back navigation if not on dashboard
  if (currentScreen != Screen.DASHBOARD) {
    BackHandler {
      currentScreen = Screen.DASHBOARD
    }
  }

  // If PIN lock is engaged, display full-screen lock
  if (isAppLocked) {
    AuthLockScreen(viewModel = viewModel)
    return
  }

  val currencySymbol = userProfile?.currencySymbol ?: "₹"

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
      ) {
        Screen.entries.forEach { screen ->
          val isSelected = currentScreen == screen
          NavigationBarItem(
            icon = {
              Icon(
                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = screen.label
              )
            },
            label = { Text(screen.label) },
            selected = isSelected,
            onClick = { currentScreen = screen },
            modifier = Modifier.testTag(screen.tag)
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
      ) { target ->
        when (target) {
          Screen.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToTransactions = { currentScreen = Screen.TRANSACTIONS },
            onNavigateToBudgets = { currentScreen = Screen.BUDGETS },
            onAddTransactionClick = { type ->
              defaultAddType = type
              editingTransaction = null
              showAddTransactionDialog = true
            },
            onEditTransactionClick = { tx ->
              editingTransaction = tx
              showAddTransactionDialog = true
            },
            onProfileClick = { showProfileDialog = true }
          )

          Screen.TRANSACTIONS -> TransactionsScreen(
            viewModel = viewModel,
            onAddTransactionClick = {
              defaultAddType = TransactionType.EXPENSE
              editingTransaction = null
              showAddTransactionDialog = true
            },
            onEditTransactionClick = { tx ->
              editingTransaction = tx
              showAddTransactionDialog = true
            }
          )

          Screen.BUDGETS -> BudgetsScreen(
            viewModel = viewModel
          )

          Screen.ANALYTICS -> AnalyticsScreen(
            viewModel = viewModel
          )
        }
      }
    }
  }

  // Add / Edit Transaction Dialog
  if (showAddTransactionDialog) {
    val initial = editingTransaction ?: TransactionEntity(
      title = "",
      amount = 0.0,
      type = defaultAddType,
      category = if (defaultAddType == TransactionType.EXPENSE) "Groceries / Ration" else "Salary"
    )

    AddEditTransactionDialog(
      initialTransaction = if (editingTransaction != null) initial else null,
      currencySymbol = currencySymbol,
      onDismiss = {
        showAddTransactionDialog = false
        editingTransaction = null
      },
      onSave = { tx ->
        if (editingTransaction != null) {
          viewModel.updateTransaction(tx)
          scope.launch {
            snackbarHostState.showSnackbar("Transaction updated")
          }
        } else {
          viewModel.addTransaction(tx)
          scope.launch {
            snackbarHostState.showSnackbar("Transaction recorded")
          }
        }
        showAddTransactionDialog = false
        editingTransaction = null
      }
    )
  }

  // User Profile Settings Dialog
  if (showProfileDialog) {
    ProfileSettingsDialog(
      userProfile = userProfile,
      viewModel = viewModel,
      onDismiss = { showProfileDialog = false }
    )
  }
}
