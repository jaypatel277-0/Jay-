package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
  initialTransaction: TransactionEntity? = null,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onSave: (TransactionEntity) -> Unit
) {
  var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
  var amountText by remember {
    mutableStateOf(if (initialTransaction != null) String.format(Locale.US, "%.2f", initialTransaction.amount) else "")
  }
  var type by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
  var selectedCategory by remember {
    mutableStateOf(
      initialTransaction?.category ?: if (type == TransactionType.EXPENSE) "Groceries / Ration" else "Salary"
    )
  }
  var selectedPaymentMethod by remember {
    mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.UPI)
  }
  var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }
  var dateMillis by remember { mutableLongStateOf(initialTransaction?.dateMillis ?: System.currentTimeMillis()) }

  var titleError by remember { mutableStateOf(false) }
  var amountError by remember { mutableStateOf(false) }

  val categories = if (type == TransactionType.EXPENSE) {
    CategoryConstants.EXPENSE_CATEGORIES
  } else {
    CategoryConstants.INCOME_CATEGORIES
  }

  val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

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
        .testTag("add_edit_transaction_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Fixed Dialog Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (initialTransaction == null) "Add Transaction" else "Edit Transaction",
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

        // Scrollable Form Body
        Column(
          modifier = Modifier
            .weight(1f, fill = false)
            .padding(horizontal = 22.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
        ) {
          // Transaction Type Toggle (Segmented Pill)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (type == TransactionType.EXPENSE) Rose500 else Color.Transparent)
                .clickable {
                  type = TransactionType.EXPENSE
                  selectedCategory = "Groceries / Ration"
                }
                .padding(vertical = 10.dp)
                .testTag("type_expense_tab"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Expense",
                fontWeight = FontWeight.Bold,
                color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (type == TransactionType.INCOME) Emerald500 else Color.Transparent)
                .clickable {
                  type = TransactionType.INCOME
                  selectedCategory = "Salary"
                }
                .padding(vertical = 10.dp)
                .testTag("type_income_tab"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Income",
                fontWeight = FontWeight.Bold,
                color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Title Input
          OutlinedTextField(
            value = title,
            onValueChange = {
              title = it
              if (it.isNotBlank()) titleError = false
            },
            label = { Text("Title / Description") },
            placeholder = { Text("e.g. DMart Groceries, Freelance UI") },
            isError = titleError,
            supportingText = if (titleError) {
              { Text("Please enter a title") }
            } else null,
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("transaction_title_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Amount Input
          OutlinedTextField(
            value = amountText,
            onValueChange = {
              amountText = it
              if (it.toDoubleOrNull() != null && it.toDouble() > 0) amountError = false
            },
            label = { Text("Amount ($currencySymbol)") },
            placeholder = { Text("0.00") },
            isError = amountError,
            supportingText = if (amountError) {
              { Text("Enter a valid amount greater than 0") }
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
              .testTag("transaction_amount_input")
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Category Selection
          Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            categories.forEach { cat ->
              val isSelected = selectedCategory == cat.name
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) BorderStroke(1.5.dp, cat.color) else null,
                onClick = { selectedCategory = cat.name },
                modifier = Modifier.testTag("cat_chip_${cat.name.replace(" ", "_")}")
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

          Spacer(modifier = Modifier.height(14.dp))

          // Payment Method
          Text(
            text = "Payment Method",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PaymentMethod.entries.forEach { method ->
              val isSelected = selectedPaymentMethod == method
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                onClick = { selectedPaymentMethod = method },
                modifier = Modifier.testTag("payment_chip_${method.name}")
              ) {
                Text(
                  text = method.displayName,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Date Selector (Today, Yesterday, 2 days ago)
          Text(
            text = "Date: ${dateFormatter.format(Date(dateMillis))}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val oneDay = 86_400_000L
            val now = System.currentTimeMillis()

            listOf(
              "Today" to now,
              "Yesterday" to now - oneDay,
              "2d ago" to now - (oneDay * 2)
            ).forEach { (label, time) ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { dateMillis = time }
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Notes Input
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            placeholder = { Text("Add any extra notes or memo") },
            maxLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("transaction_notes_input")
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Fixed Sticky Action Buttons Bar
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
                .testTag("cancel_transaction_button")
            ) {
              Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Button(
              onClick = {
                val parsedAmount = amountText.toDoubleOrNull()
                if (title.isBlank()) {
                  titleError = true
                }
                if (parsedAmount == null || parsedAmount <= 0) {
                  amountError = true
                }

                if (title.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
                  val updatedTx = initialTransaction?.copy(
                    title = title.trim(),
                    amount = parsedAmount,
                    type = type,
                    category = selectedCategory,
                    paymentMethod = selectedPaymentMethod,
                    notes = notes.trim(),
                    dateMillis = dateMillis
                  ) ?: TransactionEntity(
                    title = title.trim(),
                    amount = parsedAmount,
                    type = type,
                    category = selectedCategory,
                    paymentMethod = selectedPaymentMethod,
                    notes = notes.trim(),
                    dateMillis = dateMillis
                  )
                  onSave(updatedTx)
                }
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (type == TransactionType.EXPENSE) Rose500 else Emerald500,
                contentColor = Color.White
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("save_transaction_button")
            ) {
              Icon(
                imageVector = if (initialTransaction == null) Icons.Default.Add else Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (initialTransaction == null) "Add" else "Save Changes",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}
