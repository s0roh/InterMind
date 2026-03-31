package com.soroh.intermind.feature.statistic.impl.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soroh.intermind.core.data.dto.statistic.AggregatedModeStatistic
import com.soroh.intermind.feature.statistic.api.R
import com.soroh.intermind.feature.statistic.impl.util.toModeLabel
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle

@Composable
internal fun TrainingModesColumnChart(trainingData: List<AggregatedModeStatistic>) {
    val totalGradientStart = colorResource(id = R.color.color_total_gradient_start)
    val totalGradientEnd = colorResource(id = R.color.color_total_gradient_end)
    val correctGradientStart = colorResource(id = R.color.color_correct_gradient_start)
    val correctGradientEnd = colorResource(id = R.color.color_correct_gradient_end)
    val incorrectGradientStart = colorResource(id = R.color.color_incorrect_gradient_start)
    val incorrectGradientEnd = colorResource(id = R.color.color_incorrect_gradient_end)

    val labels = trainingData.associate {
        it.modeName to it.modeName.toModeLabel()
    }

    ColumnChart(
        modifier = Modifier
            .height(300.dp)
            .padding(20.dp),
        labelProperties = LabelProperties(
            textStyle = TextStyle.Default.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            enabled = true,
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            textStyle = TextStyle.Default.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentBuilder = { value ->
                value.toInt().toString()
            }
        ),
        dividerProperties = DividerProperties(enabled = false),
        gridProperties = GridProperties(
            xAxisProperties = GridProperties.AxisProperties(
                color = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                style = StrokeStyle.Dashed(
                    intervals = floatArrayOf(10f, 10f),
                    phase = 0f
                )
            ),
            yAxisProperties = GridProperties.AxisProperties(
                color = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                style = StrokeStyle.Dashed(
                    intervals = floatArrayOf(10f, 10f),
                    phase = 0f
                )
            )
        ),
        labelHelperProperties = LabelHelperProperties(
            textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        ),
        data = remember(trainingData) {
            trainingData.map { data ->
                Bars(
                    label = labels[data.modeName] ?: data.modeName,
                    values = listOf(
                        Bars.Data(
                            label = "Всего",
                            value = data.totalCards.toDouble(),
                            color = Brush.verticalGradient(
                                colors = listOf(totalGradientStart, totalGradientEnd)
                            )
                        ),
                        Bars.Data(
                            label = "Успешно",
                            value = data.correctAnswers.toDouble(),
                            color = Brush.verticalGradient(
                                colors = listOf(correctGradientStart, correctGradientEnd)
                            )
                        ),
                        Bars.Data(
                            label = "Ошибки",
                            value = data.incorrectAnswers.toDouble(),
                            color = Brush.verticalGradient(
                                colors = listOf(incorrectGradientStart, incorrectGradientEnd)
                            )
                        )
                    ),
                )
            }
        },
        barProperties = BarProperties(
            thickness = 15.dp,
            spacing = 6.dp,
            cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}