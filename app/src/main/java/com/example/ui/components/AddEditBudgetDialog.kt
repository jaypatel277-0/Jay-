package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BudgetEntity
import com.example.ui.theme.Emerald600
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditBudgetDialog(
  initialBudget: BudgetEntity? = null,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onSave: (BudgetEntity) -> Unit
) {
  val cal = Calendar.getInstance()
  val currentMonth = cal.get(Calendar.MONTH) + 1
  val currentYear = cal.get(Calendar.YEAR)

  var selectedCategory by remember {
    mutableStateOf(initialBudget?.category ?: "Groceries / Ration")
  }
  var limitText by remember {
    mutableStateOf(if (initialBudget != null) String.format(Locale.US, "%.2f", initialBudget.limitAmount) else "")
  }
  var limitError by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(horizontal = 16.dp, vertical = 24.dp)
        .testTag("add_edit_budget_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Fixed Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (initialBudget == null) "Set Category Budget" else "Edit Budget",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close dialog")
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f, fill = false)
            .padding(horizontal = 22.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
        ) {
          // Select Category
          Text(
            text = "Select Category",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CategoryConstants.EXPENSE_CATEGORIES.forEach { cat ->
              val isSelected = selectedCategory == cat.name
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) BorderStroke(1.5.dp, cat.color) else null,
                onClick = { selectedCategory = cat.name },
                modifier = Modifier.testTag("budget_cat_${cat.name.replace(" ", "_")}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = cat.icon,
                    contentDescription = cat.name,
                    tint = cat.color,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = cat.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Budget Limit Input
          OutlinedTextField(
            value = limitText,
            onValueChange = {
              limitText = it
              if (it.toDoubleOrNull() != null && it.toDouble() > 0) limitError = false
            },
            label = { Text("Monthly Budget Limit ($currencySymbol)") },
            placeholder = { Text("e.g. 5000.00") },
            isError = limitError,
            supportingText = if (limitError) {
              { Text("Please enter a budget limit greater than 0") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            leadingIcon = {
              Text(
                text = currencySymbol,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp)
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("budget_limit_input")
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Sticky Footer Action Bar
        Surface(
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
            ) {
              Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Button(
              onClick = {
                val parsed = limitText.toDoubleOrNull()
                if (parsed == null || parsed <= 0) {
                  limitError = true
                } else {
                  val budget = initialBudget?.copy(
                    category = selectedCategory,
                    limitAmount = parsed,
                    periodMonth = currentMonth,
                    periodYear = currentYear
                  ) ?: BudgetEntity(
                    category = selectedCategory,
                    limitAmount = parsed,
                    periodMonth = currentMonth,
                    periodYear = currentYear
                  )
                  onSave(budget)
                }
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("save_budget_button")
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save Budget", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
