package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.Emerald500
import com.example.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsDialog(
  userProfile: UserProfileEntity?,
  viewModel: ExpenseViewModel,
  onDismiss: () -> Unit
) {
  var username by remember(userProfile) { mutableStateOf(userProfile?.username ?: "Aarav Sharma") }
  var email by remember(userProfile) { mutableStateOf(userProfile?.email ?: "aarav.sharma@example.in") }
  var selectedCurrency by remember(userProfile) { mutableStateOf(userProfile?.currencySymbol ?: "₹") }

  var isPinLockEnabled by remember(userProfile) { mutableStateOf(userProfile?.isPinLockEnabled ?: false) }
  var isBiometricEnabled by remember(userProfile) { mutableStateOf(userProfile?.isBiometricEnabled ?: true) }
  var isDarkModeEnabled by remember(userProfile) { mutableStateOf(userProfile?.isDarkModeEnabled ?: false) }
  var newPin by remember { mutableStateOf("") }
  var pinError by remember { mutableStateOf(false) }

  val currencies = listOf("₹", "$", "€", "£", "AED", "SGD", "¥", "C$")

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
        .testTag("profile_settings_dialog")
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Account & Security",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        androidx.compose.material3.HorizontalDivider(
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        // Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f, fill = false)
            .padding(horizontal = 22.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
        ) {
          // Username
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_username_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Email
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_email_input")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Currency Selector
          Text(
            text = "Primary Currency (India: ₹ INR)",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            currencies.forEach { curr ->
              val isSelected = selectedCurrency == curr
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                onClick = { selectedCurrency = curr },
                modifier = Modifier.testTag("currency_chip_$curr")
              ) {
                Text(
                  text = curr,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Biometric Credentials Toggle Card
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "Biometric Authentication",
                      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Fingerprint or Face Unlock via androidx.credentials",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Switch(
                  checked = isBiometricEnabled,
                  onCheckedChange = { isBiometricEnabled = it },
                  modifier = Modifier.testTag("biometric_lock_switch")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // PIN Security Lock Section
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "App PIN Lock",
                      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Require 4-digit code on app launch",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Switch(
                  checked = isPinLockEnabled,
                  onCheckedChange = { isPinLockEnabled = it },
                  modifier = Modifier.testTag("pin_lock_switch")
                )
              }

              if (isPinLockEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                  value = newPin,
                  onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                      newPin = it
                      if (it.length == 4) pinError = false
                    }
                  },
                  label = { Text("Set 4-Digit Security PIN") },
                  placeholder = { Text("4 digits (e.g. 1234)") },
                  visualTransformation = PasswordVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                  singleLine = true,
                  isError = pinError,
                  supportingText = if (pinError) {
                    { Text("PIN must be exactly 4 digits") }
                  } else null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("set_pin_input")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Dark Theme Toggle Card
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "Dark Theme",
                      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Switch between light and dark mode",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Switch(
                  checked = isDarkModeEnabled,
                  onCheckedChange = { isDarkModeEnabled = it },
                  modifier = Modifier.testTag("dark_mode_switch")
                )
              }
            }
          }
        }

        androidx.compose.material3.HorizontalDivider(
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        // Fixed Sticky Action Bar
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
              border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
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
                if (isPinLockEnabled && userProfile?.pinHash.isNullOrBlank() && newPin.length != 4) {
                  pinError = true
                  return@Button
                }

                viewModel.saveUserProfileSettings(
                  username = username,
                  email = email,
                  currencySymbol = selectedCurrency,
                  isBiometricEnabled = isBiometricEnabled,
                  isPinLockEnabled = isPinLockEnabled,
                  isDarkModeEnabled = isDarkModeEnabled,
                  newPin = if (newPin.length == 4) newPin else null
                )

                onDismiss()
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("save_profile_settings_button")
            ) {
              Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
