package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class TrendPoint(
  val label: String,
  val amount: Double
)

data class CategorySlice(
  val category: String,
  val amount: Double,
  val percentage: Float,
  val color: Color
)

@Composable
fun SpendingTrendsLineChart(
  points: List<TrendPoint>,
  currencySymbol: String = "$",
  modifier: Modifier = Modifier,
  lineColor: Color = Rose500
) {
  if (points.isEmpty()) {
    Box(
      modifier = modifier.height(180.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No spending trend data available",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    return
  }

  var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(points) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(700))
  }

  val maxAmount = points.maxOfOrNull { it.amount }?.coerceAtLeast(10.0) ?: 100.0

  Column(modifier = modifier.fillMaxWidth()) {
    // Tooltip display on tap
    if (selectedPointIndex != null && selectedPointIndex in points.indices) {
      val pt = points[selectedPointIndex!!]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant,
          tonalElevation = 2.dp
        ) {
          Text(
            text = "${pt.label}: ${IndianCurrencyFormatter.formatInr(pt.amount, currencySymbol = currencySymbol)}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    } else {
      Spacer(modifier = Modifier.height(26.dp))
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .testTag("spending_trends_chart")
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(points) {
            detectTapGestures { offset ->
              if (points.isEmpty()) return@detectTapGestures
              val spacing = size.width / (points.size - 1).coerceAtLeast(1)
              val index = ((offset.x + spacing / 2) / spacing)
                .toInt()
                .coerceIn(0, points.size - 1)
              selectedPointIndex = if (selectedPointIndex == index) null else index
            }
          }
      ) {
        val width = size.width
        val height = size.height - 30.dp.toPx() // leave room for labels
        val topPadding = 15.dp.toPx()
        val graphHeight = height - topPadding
        val n = points.size

        // Horizontal grid guide lines
        val gridLines = 3
        for (i in 0..gridLines) {
          val y = topPadding + (graphHeight / gridLines) * i
          drawLine(
            color = Color.LightGray.copy(alpha = 0.25f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
          )
        }

        if (n == 1) {
          val pt = points.first()
          val cx = width / 2f
          val cy = topPadding + graphHeight / 2f
          drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(cx, cy))
          return@Canvas
        }

        val stepX = width / (n - 1).toFloat()
        val coords = points.mapIndexed { idx, pt ->
          val normY = (pt.amount / maxAmount).toFloat().coerceIn(0f, 1f)
          val y = topPadding + graphHeight * (1f - normY * animProgress.value)
          Offset(idx * stepX, y)
        }

        // Build smooth curve path
        val strokePath = Path()
        val fillPath = Path()

        strokePath.moveTo(coords[0].x, coords[0].y)
        fillPath.moveTo(coords[0].x, height)
        fillPath.lineTo(coords[0].x, coords[0].y)

        for (i in 0 until coords.size - 1) {
          val p0 = coords[i]
          val p1 = coords[i + 1]
          val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
          val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

          strokePath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            p1.x, p1.y
          )
          fillPath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            p1.x, p1.y
          )
        }

        fillPath.lineTo(coords.last().x, height)
        fillPath.close()

        // Gradient Fill
        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
            startY = topPadding,
            endY = height
          )
        )

        // Line Stroke
        drawPath(
          path = strokePath,
          color = lineColor,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Dots on data points
        coords.forEachIndexed { i, pt ->
          val isSelected = selectedPointIndex == i
          val radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx()
          drawCircle(
            color = if (isSelected) lineColor else Color.White,
            radius = radius,
            center = pt
          )
          drawCircle(
            color = lineColor,
            radius = radius,
            center = pt,
            style = Stroke(width = 2.dp.toPx())
          )
        }
      }
    }

    // X-axis text labels
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      points.forEachIndexed { idx, pt ->
        if (points.size <= 7 || idx == 0 || idx == points.size - 1 || idx == points.size / 2) {
          Text(
            text = pt.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  slices: List<CategorySlice>,
  totalAmount: Double,
  currencySymbol: String = "$",
  modifier: Modifier = Modifier
) {
  if (slices.isEmpty() || totalAmount <= 0.0) {
    Box(
      modifier = modifier.height(180.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No category expense data for this period",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    return
  }

  var selectedCategory by remember { mutableStateOf<String?>(null) }
  val activeSlice = slices.firstOrNull { it.category == selectedCategory }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(200.dp)
        .testTag("category_donut_chart"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(slices) {
            detectTapGestures { offset ->
              val center = Offset(size.width / 2f, size.height / 2f)
              val dx = offset.x - center.x
              val dy = offset.y - center.y
              val dist = kotlin.math.sqrt(dx * dx + dy * dy)
              val outerR = size.width / 2f
              val innerR = outerR * 0.65f

              if (dist in innerR..outerR) {
                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (angle < 0) angle += 360f

                var currentAngle = 0f
                for (slice in slices) {
                  val sweep = (slice.percentage / 100f) * 360f
                  if (angle in currentAngle..(currentAngle + sweep)) {
                    selectedCategory = if (selectedCategory == slice.category) null else slice.category
                    break
                  }
                  currentAngle += sweep
                }
              } else {
                selectedCategory = null
              }
            }
          }
      ) {
        val strokeWidth = 24.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

        var startAngle = -90f
        for (slice in slices) {
          val sweep = (slice.percentage / 100f) * 360f
          val isSelected = selectedCategory == slice.category
          val widthToUse = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth

          drawArc(
            color = slice.color,
            startAngle = startAngle,
            sweepAngle = sweep.coerceAtLeast(1.5f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = widthToUse, cap = StrokeCap.Butt)
          )
          startAngle += sweep
        }
      }

      // Center text
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (activeSlice != null) activeSlice.category else "Total Spent",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
        Text(
          text = if (activeSlice != null) {
            IndianCurrencyFormatter.formatInr(activeSlice.amount, currencySymbol = currencySymbol)
          } else {
            IndianCurrencyFormatter.formatInr(totalAmount, currencySymbol = currencySymbol)
          },
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        if (activeSlice != null) {
          Text(
            text = "${String.format(Locale.US, "%.1f", activeSlice.percentage)}%",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = activeSlice.color
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Legend Chips
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      slices.forEach { slice ->
        val isSelected = selectedCategory == slice.category
        Surface(
          shape = CircleShape,
          color = if (isSelected) slice.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
          border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, slice.color) else null,
          onClick = {
            selectedCategory = if (selectedCategory == slice.category) null else slice.category
          },
          modifier = Modifier.padding(horizontal = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .drawBehind { drawCircle(color = slice.color) }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = slice.category,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${String.format(Locale.US, "%.0f", slice.percentage)}%",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
