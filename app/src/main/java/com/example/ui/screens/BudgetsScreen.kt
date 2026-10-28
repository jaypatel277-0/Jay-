package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BudgetEntity
import com.example.ui.components.AddEditBudgetDialog
import com.example.ui.components.CategoryConstants
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import com.example.ui.viewmodel.BudgetProgress
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun BudgetsScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

  var showAddDialog by remember { mutableStateOf(false) }
  var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }

  val currencySymbol = userProfile?.currencySymbol ?: "₹"

  val totalBudgetLimit = budgetProgressList.sumOf { it.budget.limitAmount }
  val totalBudgetSpent = budgetProgressList.sumOf { it.spentAmount }
  val totalUtilized = if (totalBudgetLimit > 0) ((totalBudgetSpent / totalBudgetLimit) * 100).toFloat() else 0f

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("budgets_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header
      item {
        Column {
          Text(
            text = "Budgets & Limits",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Monitor category spending caps with smart alerts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Overall Monthly Budget Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("total_budget_card"),
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Total Monthly Allocation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Across ${budgetProgressList.size} categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                  totalUtilized >= 90f -> Rose500.copy(alpha = 0.15f)
                  totalUtilized >= 75f -> Amber500.copy(alpha = 0.15f)
                  else -> Emerald500.copy(alpha = 0.15f)
                }
              ) {
                Text(
                  text = "${String.format(Locale.US, "%.0f", totalUtilized)}% used",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = when {
                    totalUtilized >= 90f -> Rose500
                    totalUtilized >= 75f -> Amber500
                    else -> Emerald500
                  },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Bar
            val animatedTotalProgress by animateFloatAsState(
              targetValue = (totalUtilized / 100f).coerceIn(0f, 1f),
              label = "total_progress"
            )

            LinearProgressIndicator(
              progress = { animatedTotalProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
              color = when {
                totalUtilized >= 90f -> Rose500
                totalUtilized >= 75f -> Amber500
                else -> Emerald500
              },
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Spent: ${IndianCurrencyFormatter.formatInr(totalBudgetSpent, currencySymbol = currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Limit: ${IndianCurrencyFormatter.formatInr(totalBudgetLimit, currencySymbol = currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Category Budgets Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Category Limits",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Text(
            text = "+ Set Category",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .clickable { showAddDialog = true }
              .padding(4.dp)
              .testTag("set_category_budget_button")
          )
        }
      }

      // Empty State
      if (budgetProgressList.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No Budgets Set",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Set category limits to track and prevent overspending",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("create_first_budget_button")
              ) {
                Text("Set First Category Budget")
              }
            }
          }
        }
      } else {
        items(budgetProgressList, key = { it.budget.id }) { item ->
          CategoryBudgetItemCard(
            progress = item,
            currencySymbol = currencySymbol,
            onEdit = { editingBudget = item.budget },
            onDelete = { viewModel.deleteBudget(item.budget) }
          )
        }
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = { showAddDialog = true },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 16.dp)
        .testTag("add_budget_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Set Budget")
    }
  }

  // Add Dialog
  if (showAddDialog) {
    AddEditBudgetDialog(
      currencySymbol = currencySymbol,
      onDismiss = { showAddDialog = false },
      onSave = { budget ->
        viewModel.saveBudget(budget)
        showAddDialog = false
      }
    )
  }

  // Edit Dialog
  if (editingBudget != null) {
    AddEditBudgetDialog(
      initialBudget = editingBudget,
      currencySymbol = currencySymbol,
      onDismiss = { editingBudget = null },
      onSave = { budget ->
        viewModel.saveBudget(budget)
        editingBudget = null
      }
    )
  }
}

@Composable
fun CategoryBudgetItemCard(
  progress: BudgetProgress,
  currencySymbol: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val meta = CategoryConstants.getCategoryMeta(progress.budget.category)
  val pct = progress.percentUtilized

  val statusColor = when {
    pct >= 90f -> Rose500
    pct >= 75f -> Amber500
    else -> Emerald500
  }

  val statusText = when {
    progress.isExceeded -> "Over Budget by ${IndianCurrencyFormatter.formatInr(progress.spentAmount - progress.budget.limitAmount, currencySymbol = currencySymbol)}"
    pct >= 90f -> "Critical: ${String.format(Locale.US, "%.0f", pct)}% used"
    pct >= 75f -> "Warning: ${String.format(Locale.US, "%.0f", pct)}% used"
    else -> "Healthy: ${String.format(Locale.US, "%.0f", pct)}% used"
  }

  val statusIcon = when {
    pct >= 90f -> Icons.Default.Warning
    pct >= 75f -> Icons.Default.NotificationsActive
    else -> Icons.Default.CheckCircle
  }

  val animatedProgress by animateFloatAsState(
    targetValue = (pct / 100f).coerceIn(0f, 1f),
    label = "cat_budget_progress"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("budget_card_${progress.budget.category.replace(" ", "_")}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(meta.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = meta.icon,
              contentDescription = progress.budget.category,
              tint = meta.color,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = progress.budget.category,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${IndianCurrencyFormatter.formatInr(progress.spentAmount, currencySymbol = currencySymbol)} of ${IndianCurrencyFormatter.formatInr(progress.budget.limitAmount, currencySymbol = currencySymbol)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Quick edit & delete
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier
              .size(32.dp)
              .testTag("edit_budget_${progress.budget.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit budget",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(32.dp)
              .testTag("delete_budget_${progress.budget.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete budget",
              tint = Rose500,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Linear progress bar
      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Status indicator badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = statusColor.copy(alpha = 0.12f)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Icon(
              imageVector = statusIcon,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = statusText,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = statusColor
            )
          }
        }

        val remaining = progress.budget.limitAmount - progress.spentAmount
        Text(
          text = if (remaining >= 0) {
            "${IndianCurrencyFormatter.formatInr(remaining, currencySymbol = currencySymbol)} left"
          } else {
            "-${IndianCurrencyFormatter.formatInr(kotlin.math.abs(remaining), currencySymbol = currencySymbol)}"
          },
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Rose500
        )
      }
    }
  }
}
