package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun AuthLockScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val biometricStatus by viewModel.biometricStatusMessage.collectAsStateWithLifecycle()

  var enteredPin by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Prompt biometric authentication on initial display if enabled
  LaunchedEffect(userProfile) {
    if (userProfile?.isBiometricEnabled == true && activity != null) {
      viewModel.authenticateWithBiometrics(activity)
    }
  }

  fun handleDigit(digit: String) {
    if (enteredPin.length < 4) {
      val newPin = enteredPin + digit
      enteredPin = newPin
      errorMessage = null
      if (newPin.length == 4) {
        val success = viewModel.unlockWithPin(newPin)
        if (!success) {
          errorMessage = "Incorrect PIN code. Please try again."
          enteredPin = ""
        }
      }
    }
  }

  fun handleBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      errorMessage = null
    }
  }

  Surface(
    modifier = modifier
      .fillMaxSize()
      .testTag("auth_lock_screen"),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Lock Icon & Greeting
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(Emerald500, Indigo500))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Secure Expense Tracker",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Biometric & PIN security for ${userProfile?.username ?: "User"}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4 PIN Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 0 until 4) {
            val isFilled = i < enteredPin.length
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                  if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AnimatedVisibility(visible = errorMessage != null || biometricStatus != null) {
          Text(
            text = errorMessage ?: biometricStatus ?: "",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (errorMessage != null) Rose500 else MaterialTheme.colorScheme.primary
          )
        }
      }

      // Biometric Quick Unlock Card Button
      if (activity != null) {
        Button(
          onClick = { viewModel.authenticateWithBiometrics(activity) },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .testTag("biometric_credential_unlock_button")
        ) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Biometric",
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Unlock with Biometrics",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }

      // Numeric Keypad Grid (3x4)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("BIO", "0", "DEL")
        )

        rows.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            row.forEach { key ->
              when (key) {
                "BIO" -> {
                  IconButton(
                    onClick = {
                      if (activity != null) {
                        viewModel.authenticateWithBiometrics(activity)
                      } else {
                        viewModel.unlockWithPin(userProfile?.pinHash ?: "")
                      }
                    },
                    modifier = Modifier
                      .size(64.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .testTag("biometric_keypad_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Fingerprint,
                      contentDescription = "Biometric unlock",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(28.dp)
                    )
                  }
                }
                "DEL" -> {
                  IconButton(
                    onClick = { handleBackspace() },
                    modifier = Modifier
                      .size(64.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .testTag("pin_backspace_button")
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Backspace,
                      contentDescription = "Backspace",
                      tint = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                else -> {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp,
                    onClick = { handleDigit(key) },
                    modifier = Modifier
                      .size(64.dp)
                      .testTag("pin_key_$key")
                  ) {
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier.fillMaxSize()
                    ) {
                      Text(
                        text = key,
                        style = MaterialTheme.typography.titleLarge.copy(
                          fontSize = 22.sp,
                          fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Bottom Reset PIN
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        TextButton(
          onClick = { viewModel.updatePinSettings(null, false) },
          modifier = Modifier.testTag("reset_pin_button")
        ) {
          Text(
            text = "Forgot PIN? Reset Lock",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
