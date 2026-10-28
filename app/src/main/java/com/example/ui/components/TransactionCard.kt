package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionCard(
  transaction: TransactionEntity,
  currencySymbol: String = "₹",
  onItemClick: () -> Unit = {},
  onEditClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  showActions: Boolean = true,
  modifier: Modifier = Modifier
) {
  val meta = CategoryConstants.getCategoryMeta(transaction.category)
  val isIncome = transaction.type == TransactionType.INCOME
  val amountColor = if (isIncome) Emerald500 else Rose500
  val sign = if (isIncome) "+" else "-"

  val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
  val dateString = dateFormatter.format(Date(transaction.dateMillis))

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("transaction_item_${transaction.id}")
      .clickable(onClick = onItemClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Icon Badge
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(meta.color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = meta.icon,
          contentDescription = transaction.category,
          tint = meta.color,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Title & Subtitle Info
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = transaction.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = dateString,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = transaction.paymentMethod.displayName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }

        if (transaction.notes.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = transaction.notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Amount & Quick Actions
      Column(
        horizontalAlignment = Alignment.End
      ) {
        Text(
          text = "$sign${IndianCurrencyFormatter.formatInr(transaction.amount, includeSymbol = true, currencySymbol = currencySymbol)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = amountColor
        )

        if (showActions) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
          ) {
            IconButton(
              onClick = onEditClick,
              modifier = Modifier
                .size(32.dp)
                .testTag("edit_tx_${transaction.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit transaction",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
            IconButton(
              onClick = onDeleteClick,
              modifier = Modifier
                .size(32.dp)
                .testTag("delete_tx_${transaction.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete transaction",
                tint = Rose500,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
