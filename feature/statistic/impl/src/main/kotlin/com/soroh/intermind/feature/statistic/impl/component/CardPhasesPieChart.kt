package com.soroh.intermind.feature.statistic.impl.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soroh.intermind.core.data.dto.statistic.CardPhase
import com.soroh.intermind.core.data.dto.statistic.CardPhaseStat
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
internal fun CardPhasesPieChart(
    phasesStats: List<CardPhaseStat>
) {
    val newColor = Color(0xFF2196F3)
    val learningColor = Color(0xFFFF9800)
    val graduatedColor = Color(0xFF4CAF50)

    var pieData by remember(phasesStats) {
        mutableStateOf(
            phasesStats.map { stat ->
                Pie(
                    label = when (stat.phase) {
                        CardPhase.NEW -> "Новые"
                        CardPhase.LEARNING -> "Изучаемые"
                        CardPhase.GRADUATED -> "Закрепленные"
                    },
                    data = stat.count.toDouble(),
                    color = when (stat.phase) {
                        CardPhase.NEW -> newColor
                        CardPhase.LEARNING -> learningColor
                        CardPhase.GRADUATED -> graduatedColor
                    },
                    selectedColor = when (stat.phase) {
                        CardPhase.NEW -> newColor.copy(alpha = 0.8f)
                        CardPhase.LEARNING -> learningColor.copy(alpha = 0.8f)
                        CardPhase.GRADUATED -> graduatedColor.copy(alpha = 0.8f)
                    }
                )
            }.filter { it.data > 0 }
        )
    }

    val selectedPie = pieData.find { it.selected }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp),
            data = pieData,
            onPieClick = { clickedPie ->
                pieData = pieData.map {
                    it.copy(selected = it == clickedPie && !it.selected)
                }
            },
            selectedScale = 1.2f,
            style = Pie.Style.Stroke(width = 50.dp),
            scaleAnimEnterSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            colorAnimEnterSpec = tween(300),
            colorAnimExitSpec = tween(300),
            scaleAnimExitSpec = tween(300),
            spaceDegreeAnimExitSpec = tween(300),
            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = TextStyle.Default.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        )

        AnimatedVisibility(visible = selectedPie != null) {
            selectedPie?.let {
                Text(
                    text = "${it.label}: ${it.data.toInt()} шт",
                    style = MaterialTheme.typography.titleMedium,
                    color = it.color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}