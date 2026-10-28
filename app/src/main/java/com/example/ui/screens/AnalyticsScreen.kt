package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryConstants
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val momStats by viewModel.monthOverMonthStats.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val categorySlices by viewModel.categorySlices.collectAsStateWithLifecycle()

  var showCsvDialog by remember { mutableStateOf(false) }
  var copiedToast by remember { mutableStateOf(false) }

  val currencySymbol = userProfile?.currencySymbol ?: "₹"

  // Payment method breakdown
  val expenses = allTransactions.filter { it.type == TransactionType.EXPENSE }
  val totalExpenses = expenses.sumOf { it.amount }
  val paymentBreakdown = expenses
    .groupBy { it.paymentMethod }
    .map { (method, list) ->
      val sum = list.sumOf { it.amount }
      val pct = if (totalExpenses > 0) ((sum / totalExpenses) * 100).toFloat() else 0f
      Triple(method.displayName, sum, pct)
    }.sortedByDescending { it.second }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("analytics_screen"),
    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "Financial Analytics",
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Month-over-month patterns and data export",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Month-Over-Month Comparison Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("mom_comparison_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Insights,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Month-Over-Month",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "This Month vs Last Month",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // 2 Metric Columns
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Expense MoM
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Expenses MoM",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = IndianCurrencyFormatter.formatInr(momStats.thisMonthExpense, currencySymbol = currencySymbol),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  val isExpenseHigher = momStats.expenseChangePercent > 0
                  Icon(
                    imageVector = if (isExpenseHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isExpenseHigher) Rose500 else Emerald500,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${if (momStats.expenseChangePercent > 0) "+" else ""}${String.format(Locale.US, "%.1f", momStats.expenseChangePercent)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isExpenseHigher) Rose500 else Emerald500
                  )
                }
                Text(
                  text = "vs ${IndianCurrencyFormatter.formatInr(momStats.lastMonthExpense, currencySymbol = currencySymbol)} last mo",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Income MoM
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Income MoM",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = IndianCurrencyFormatter.formatInr(momStats.thisMonthIncome, currencySymbol = currencySymbol),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  val isIncomeHigher = momStats.incomeChangePercent >= 0
                  Icon(
                    imageVector = if (isIncomeHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isIncomeHigher) Emerald500 else Rose500,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${if (momStats.incomeChangePercent > 0) "+" else ""}${String.format(Locale.US, "%.1f", momStats.incomeChangePercent)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isIncomeHigher) Emerald500 else Rose500
                  )
                }
                Text(
                  text = "vs ${IndianCurrencyFormatter.formatInr(momStats.lastMonthIncome, currencySymbol = currencySymbol)} last mo",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Daily Average Spending
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Daily Average Spending",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Based on current month days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Text(
                text = "${IndianCurrencyFormatter.formatInr(momStats.dailyAverageExpense, currencySymbol = currencySymbol)}/day",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    // Payment Method Breakdown
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("payment_methods_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Payment,
              contentDescription = null,
              tint = Indigo500,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Spending by Payment Method",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          paymentBreakdown.forEach { (name, amount, pct) ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${IndianCurrencyFormatter.formatInr(amount, currencySymbol = currencySymbol)} (${String.format(Locale.US, "%.0f", pct)}%)",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { (pct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = Indigo500,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }
        }
      }
    }

    // Export Transaction History to CSV Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("csv_export_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Emerald500.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = Emerald500,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Export Transaction Data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${allTransactions.size} total records ready to export",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Download or share your entire financial ledger in standard CSV format for external analysis in Google Sheets, Microsoft Excel, or personal backup.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { showCsvDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("preview_csv_button")
            ) {
              Text("Preview CSV")
            }

            Button(
              onClick = {
                val csv = viewModel.generateCsvContent()
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, csv)
                  type = "text/csv"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Expenses CSV")
                context.startActivity(shareIntent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
              modifier = Modifier
                .weight(1f)
                .testTag("share_csv_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Export & Share", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // CSV Preview & Copy Dialog
  if (showCsvDialog) {
    val csv = viewModel.generateCsvContent()
    Dialog(onDismissRequest = { showCsvDialog = false }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .testTag("csv_preview_dialog")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CSV Export Preview",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { showCsvDialog = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Code-styled box
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp)
          ) {
            Text(
              text = csv,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                clipboardManager.setText(AnnotatedString(csv))
                copiedToast = true
              },
              modifier = Modifier
                .weight(1f)
                .testTag("copy_csv_button")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (copiedToast) "Copied!" else "Copy CSV")
            }

            Button(
              onClick = {
                showCsvDialog = false
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, csv)
                  type = "text/csv"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Expenses CSV")
                context.startActivity(shareIntent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share", color = Color.White)
            }
          }
        }
      }
    }
  }
}
