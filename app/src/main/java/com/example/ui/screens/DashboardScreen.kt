package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.SpendingTrendsLineChart
import com.example.ui.components.TransactionCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.viewmodel.DateRangeFilter
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: ExpenseViewModel,
  onNavigateToTransactions: () -> Unit,
  onNavigateToBudgets: () -> Unit,
  onAddTransactionClick: (TransactionType) -> Unit,
  onEditTransactionClick: (TransactionEntity) -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val categorySlices by viewModel.categorySlices.collectAsStateWithLifecycle()
  val trendPoints by viewModel.spendingTrendPoints.collectAsStateWithLifecycle()
  val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
  val selectedDateRange by viewModel.dateRangeFilter.collectAsStateWithLifecycle()

  val exceededBudgets = budgetProgressList.filter { it.isExceeded }
  val warningBudgets = budgetProgressList.filter { it.isWarning }

  val curr = userProfile?.currencySymbol ?: "₹"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    // Top Bar / User Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable(onClick = onProfileClick)
            .testTag("user_profile_header")
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(Emerald500, Indigo500))
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "User Profile",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Namaste,",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = userProfile?.username ?: "Aarav Sharma",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Text(
              text = curr,
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          if (userProfile?.isPinLockEnabled == true) {
            IconButton(
              onClick = { viewModel.lockApp() },
              modifier = Modifier.testTag("lock_app_button")
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock app",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Budget Alerts Banner
    if (exceededBudgets.isNotEmpty() || warningBudgets.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onNavigateToBudgets)
            .testTag("budget_alert_banner"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (exceededBudgets.isNotEmpty()) Rose500.copy(alpha = 0.12f) else Amber500.copy(alpha = 0.12f)
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (exceededBudgets.isNotEmpty()) Rose500.copy(alpha = 0.4f) else Amber500.copy(alpha = 0.4f)
          )
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (exceededBudgets.isNotEmpty()) Icons.Default.Warning else Icons.Default.NotificationsActive,
              contentDescription = "Alert",
              tint = if (exceededBudgets.isNotEmpty()) Rose500 else Amber500,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (exceededBudgets.isNotEmpty()) {
                  "Budget Alert: ${exceededBudgets.first().budget.category} exceeded!"
                } else {
                  "Warning: ${warningBudgets.first().budget.category} is near limit (${String.format(Locale.US, "%.0f", warningBudgets.first().percentUtilized)}%)"
                },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (exceededBudgets.isNotEmpty()) Rose500 else Amber500
              )
              Text(
                text = "Tap to review monthly category allocations",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Go to budgets",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // Date Range Filter Chips
    item {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(DateRangeFilter.entries) { filter ->
          val isSelected = selectedDateRange == filter
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            onClick = { viewModel.setDateRangeFilter(filter) },
            tonalElevation = if (isSelected) 2.dp else 1.dp,
            modifier = Modifier.testTag("filter_chip_${filter.name}")
          ) {
            Text(
              text = filter.displayName,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              ),
              color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
          }
        }
      }
    }

    // Financial Overview Hero Card (Indian Rupee Formatting)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("net_balance_card"),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(
                  listOf(Emerald500.copy(alpha = 0.08f), Color.Transparent)
                )
              )
              .padding(20.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Emerald500,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Current Net Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (summary.netBalance >= 0) Emerald500.copy(alpha = 0.15f) else Rose500.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = if (summary.netBalance >= 0) "Surplus" else "Deficit",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (summary.netBalance >= 0) Emerald500 else Rose500,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = IndianCurrencyFormatter.formatInr(summary.netBalance, currencySymbol = curr),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Income vs Expenses in INR (Lakhs & Crores formatting)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Total Income
                Column(modifier = Modifier.weight(1.1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                      contentDescription = null,
                      tint = Emerald500,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Total Income",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Text(
                    text = "+${IndianCurrencyFormatter.formatInr(summary.totalIncome, currencySymbol = curr)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Emerald500
                  )
                }

                // Total Expense
                Column(modifier = Modifier.weight(1.1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                      contentDescription = null,
                      tint = Rose500,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Total Expenses",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Text(
                    text = "-${IndianCurrencyFormatter.formatInr(summary.totalExpense, currencySymbol = curr)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Rose500
                  )
                }

                // Monthly Savings Rate
                Column(modifier = Modifier.weight(0.9f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Percent,
                      contentDescription = null,
                      tint = Indigo500,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Savings Rate",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Text(
                    text = "${String.format(Locale.US, "%.1f", summary.savingsRate)}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Indigo500
                  )
                }
              }
            }
          }
        }
      }
    }

    // Quick Action Buttons
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
          colors = ButtonDefaults.buttonColors(containerColor = Rose500),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("quick_add_expense_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("+ Expense", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = { onAddTransactionClick(TransactionType.INCOME) },
          colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("quick_add_income_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("+ Income", fontWeight = FontWeight.Bold)
        }
      }
    }

    // Visual Charts Section
    // 1. Spending Trends Line Chart Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
          .testTag("spending_trends_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = Rose500,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Monthly Spending Trends",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = selectedDateRange.displayName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          SpendingTrendsLineChart(
            points = trendPoints,
            currencySymbol = curr
          )
        }
      }
    }

    // 2. Category Breakdown Donut Chart Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
          .testTag("category_breakdown_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                tint = Indigo500,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Category-Wise Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = "${categorySlices.size} Categories",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          CategoryDonutChart(
            slices = categorySlices,
            totalAmount = summary.totalExpense,
            currencySymbol = curr
          )
        }
      }
    }

    // Recent Transactions Section Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Transactions",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable(onClick = onNavigateToTransactions)
            .testTag("view_all_transactions_button")
        ) {
          Text(
            text = "View All",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Recent Transactions List
    val recentTransactions = filteredTransactions.take(5)
    if (recentTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No transactions recorded for this period",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
              onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
              modifier = Modifier.testTag("add_first_transaction_button")
            ) {
              Text("Add First Transaction")
            }
          }
        }
      }
    } else {
      items(recentTransactions, key = { it.id }) { tx ->
        TransactionCard(
          transaction = tx,
          currencySymbol = curr,
          onEditClick = { onEditTransactionClick(tx) },
          onDeleteClick = { viewModel.deleteTransaction(tx) },
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
      }
    }
  }
}
