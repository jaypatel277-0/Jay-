package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryConstants
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.TransactionCard
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import com.example.ui.viewmodel.DateRangeFilter
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun TransactionsScreen(
  viewModel: ExpenseViewModel,
  onAddTransactionClick: () -> Unit,
  onEditTransactionClick: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
  val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
  val paymentMethodFilter by viewModel.paymentMethodFilter.collectAsStateWithLifecycle()
  val dateRangeFilter by viewModel.dateRangeFilter.collectAsStateWithLifecycle()
  val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

  var showSortMenu by remember { mutableStateOf(false) }

  val currencySymbol = userProfile?.currencySymbol ?: "₹"

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("transactions_screen")
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // Header & Search Bar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val context = LocalContext.current
            // Export CSV Button
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              onClick = { viewModel.exportCsvAndShare(context) },
              modifier = Modifier.testTag("export_csv_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Export CSV",
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Export",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }

            // Sort Button with dropdown
            Box {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { showSortMenu = true },
                modifier = Modifier.testTag("sort_dropdown_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = sortOption.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }

            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = { showSortMenu = false }
            ) {
              SortOption.entries.forEach { opt ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = opt.displayName,
                      fontWeight = if (sortOption == opt) FontWeight.Bold else FontWeight.Normal,
                      color = if (sortOption == opt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  },
                  onClick = {
                    viewModel.setSortOption(opt)
                    showSortMenu = false
                  }
                )
              }
            }
          }
        }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Search transactions, notes, or categories...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_transactions_input")
        )
      }

      // Filter Chips Row
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Type filter chips
        item {
          val isAll = typeFilter == null
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isAll) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            onClick = { viewModel.setTypeFilter(null) },
            modifier = Modifier.testTag("filter_type_all")
          ) {
            Text(
              text = "All Types",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal),
              color = if (isAll) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        item {
          val isExpense = typeFilter == TransactionType.EXPENSE
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isExpense) Rose500.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            border = if (isExpense) androidx.compose.foundation.BorderStroke(1.dp, Rose500) else null,
            onClick = { viewModel.setTypeFilter(if (isExpense) null else TransactionType.EXPENSE) },
            modifier = Modifier.testTag("filter_type_expense")
          ) {
            Text(
              text = "Expenses",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal),
              color = if (isExpense) Rose500 else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        item {
          val isIncome = typeFilter == TransactionType.INCOME
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isIncome) Emerald500.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            border = if (isIncome) androidx.compose.foundation.BorderStroke(1.dp, Emerald500) else null,
            onClick = { viewModel.setTypeFilter(if (isIncome) null else TransactionType.INCOME) },
            modifier = Modifier.testTag("filter_type_income")
          ) {
            Text(
              text = "Income",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Normal),
              color = if (isIncome) Emerald500 else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        // Date Range Selector Chip
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = {
              val next = when (dateRangeFilter) {
                DateRangeFilter.THIS_MONTH -> DateRangeFilter.LAST_MONTH
                DateRangeFilter.LAST_MONTH -> DateRangeFilter.THIS_YEAR
                DateRangeFilter.THIS_YEAR -> DateRangeFilter.ALL_TIME
                DateRangeFilter.ALL_TIME -> DateRangeFilter.THIS_MONTH
              }
              viewModel.setDateRangeFilter(next)
            },
            modifier = Modifier.testTag("filter_date_range")
          ) {
            Text(
              text = "📅 ${dateRangeFilter.displayName}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        // Category Filter Clear chip if active
        if (categoryFilter != null) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              onClick = { viewModel.setCategoryFilter(null) }
            ) {
              Text(
                text = "Cat: $categoryFilter ✕",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Payment Method Filter Clear chip if active
        if (paymentMethodFilter != null) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              onClick = { viewModel.setPaymentMethodFilter(null) }
            ) {
              Text(
                text = "Pay: ${paymentMethodFilter?.displayName} ✕",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // Result Count & Summary bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${transactions.size} transactions found",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val totalAmount = transactions.fold(0.0) { acc, tx ->
          if (tx.type == TransactionType.INCOME) acc + tx.amount else acc - tx.amount
        }

        Text(
          text = "Net: ${IndianCurrencyFormatter.formatInr(totalAmount, currencySymbol = currencySymbol)}",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = if (totalAmount >= 0) Emerald500 else Rose500
        )
      }

      // Transactions List
      if (transactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No matching transactions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Try adjusting your filters or search keywords",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = { viewModel.clearAllFilters() }
              ) {
                Text(
                  text = "Reset All Filters",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 88.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(transactions, key = { it.id }) { tx ->
            TransactionCard(
              transaction = tx,
              currencySymbol = currencySymbol,
              onEditClick = { onEditTransactionClick(tx) },
              onDeleteClick = { viewModel.deleteTransaction(tx) }
            )
          }
        }
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = onAddTransactionClick,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 16.dp)
        .testTag("add_transaction_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Transaction")
    }
  }
}
