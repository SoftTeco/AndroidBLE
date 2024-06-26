package com.softteco.template.ui.feature.deviceDashboard.devices.thermometer

import android.graphics.PorterDuff
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SevereCold
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.component
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shader.TopBottomShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.softteco.template.R
import com.softteco.template.data.deviceData.ThermometerData
import com.softteco.template.data.deviceData.ThermometerValues
import com.softteco.template.ui.components.DashboardValueBlock
import com.softteco.template.ui.components.DeviceDashboardTopAppBar
import com.softteco.template.ui.components.rememberMarker
import com.softteco.template.ui.feature.deviceDashboard.devices.thermometer.ThermometerDashboardViewModel.TimeIntervalMenu
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

@Composable
fun ThermometerDashboardScreen(
    onSettingsClick: (deviceId: String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThermometerDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onDeviceResultCallback { macAddress ->
            scope.launch {
                viewModel.getThermometerValues(macAddress)
            }
        }
    }

    ScreenContent(
        state,
        toLocalDate = { viewModel.toLocalDate(it) },
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        onBackClicked = onBackClicked,
        updateUnit = { unit -> viewModel.unit = unit }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    state: ThermometerDashboardViewModel.State,
    onSettingsClick: (deviceId: String) -> Unit,
    toLocalDate: (values: List<ThermometerValues>) -> Map<LocalDate, Double>,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
    updateUnit: (unit: TimeIntervalMenu) -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingDefault),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var timeIntervalMenu by rememberSaveable { mutableStateOf(TimeIntervalMenu.Minute) }
        DeviceDashboardTopAppBar(
            state.thermometerData.deviceName,
            onSettingsClick = { onSettingsClick(state.thermometerData.deviceId.toString()) },
            modifier = Modifier.fillMaxWidth(),
            onBackClicked = onBackClicked
        )
        Row(
            modifier = Modifier
                .padding(horizontal = Dimens.PaddingSmall)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardValueBlock(
                value = state.thermometerData.currentTemperature,
                valueName = stringResource(R.string.temperature),
                measurementUnit = stringResource(R.string.degrees_celsius_icon),
                icon = Icons.Filled.SevereCold,
                modifier = Modifier.weight(1f)
            )
            DashboardValueBlock(
                value = state.thermometerData.currentHumidity,
                valueName = stringResource(R.string.humidity),
                measurementUnit = stringResource(R.string.percent_icon),
                icon = Icons.Outlined.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = Dimens.PaddingDefault)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SingleChoiceSegmentedButtonRow(
                Modifier.fillMaxWidth()
            ) {
                TimeIntervalMenu.entries.forEachIndexed { index, segmentUIFramework ->
                    SegmentedButton(
                        selected = timeIntervalMenu == segmentUIFramework,
                        onClick = {
                            timeIntervalMenu = segmentUIFramework
                            updateUnit(timeIntervalMenu)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index,
                            TimeIntervalMenu.entries.size
                        ),
                    ) {
                        Text(stringResource(segmentUIFramework.labelResourceID))
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = Dimens.PaddingSmall)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ElevatedCard(modifier = Modifier.padding(horizontal = Dimens.PaddingSmall)) {
                if (timeIntervalMenu.chronoUnit == ChronoUnit.MINUTES ||
                    timeIntervalMenu.chronoUnit == ChronoUnit.HOURS
                ) {
                    SetGraphData(
                        values = state.thermometerData.valuesHistory
                            .map {
                                (it as ThermometerValues.DataLYWSD03MMC).let {
                                    it.timestamp to it.temperature
                                }
                            }.toMap(),
                        bottomAxisValueFormatter = state.bottomAxisValueFormatter,
                        timeUnit = timeIntervalMenu.chronoUnit
                    )
                } else {
                    SetGraphData(
                        values = toLocalDate(state.thermometerData.valuesHistory),
                        bottomAxisValueFormatter = state.bottomAxisValueFormatter,
                        timeUnit = timeIntervalMenu.chronoUnit
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SetGraphData(
    values: Map<T, Double>,
    bottomAxisValueFormatter: CartesianValueFormatter,
    timeUnit: ChronoUnit,
    modifier: Modifier = Modifier,
) where T : Temporal, T : Comparable<T> {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    LaunchedEffect(values) {
        withContext(Dispatchers.Default) {
            val minDateTime = values.keys.minOrNull() ?: when (timeUnit) {
                ChronoUnit.DAYS, ChronoUnit.MONTHS -> LocalDate.now() as T
                else -> LocalDateTime.now() as T
            }

            val xToDates = values.keys.associateBy {
                when (timeUnit) {
                    ChronoUnit.MINUTES, ChronoUnit.HOURS ->
                        timeUnit.between(minDateTime as LocalDateTime, it as LocalDateTime)
                            .toFloat()

                    ChronoUnit.DAYS, ChronoUnit.MONTHS ->
                        timeUnit.between(minDateTime as LocalDate, it as LocalDate).toFloat()

                    else -> 0f
                }
            }

            modelProducer.tryRunTransaction {
                lineSeries { series(xToDates.keys, values.values) }
                updateExtras {
                    when (timeUnit) {
                        ChronoUnit.MINUTES, ChronoUnit.HOURS ->
                            it[xToDateMapKeyLocalDateTime] = xToDates as Map<Float, LocalDateTime>

                        ChronoUnit.DAYS, ChronoUnit.MONTHS ->
                            it[xToDateMapKeyLocalDate] = xToDates as Map<Float, LocalDate>

                        else -> {}
                    }
                }
            }
        }
    }
    
    Graph(modelProducer, bottomAxisValueFormatter, modifier)
}

@Composable
private fun Graph(
    modelProducer: CartesianChartModelProducer,
    bottomAxisValueFormatter: CartesianValueFormatter,
    modifier: Modifier = Modifier
) {
    val marker = rememberMarker()
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lines =
                listOf(
                    rememberLineSpec(
                        shader =
                        TopBottomShader(
                            DynamicShader.color(MaterialTheme.colorScheme.primary),
                            DynamicShader.color(MaterialTheme.colorScheme.error)
                        ),
                        backgroundShader =
                        TopBottomShader(
                            DynamicShader.compose(
                                DynamicShader.component(
                                    componentSize = 6.dp,
                                    component =
                                    rememberShapeComponent(
                                        shape = Shape.Pill,
                                        color = MaterialTheme.colorScheme.primary,
                                        margins = Dimensions.of(1.dp),
                                    ),
                                ),
                                DynamicShader.verticalGradient(
                                    arrayOf(
                                        Color.Black,
                                        Color.Transparent
                                    )
                                ),
                                PorterDuff.Mode.DST_IN,
                            ),
                            DynamicShader.compose(
                                DynamicShader.component(
                                    componentSize = 5.dp,
                                    component =
                                    rememberShapeComponent(
                                        shape = Shape.Rectangle,
                                        color = MaterialTheme.colorScheme.error,
                                        margins = Dimensions.of(horizontal = 2.dp),
                                    ),
                                    checkeredArrangement = false,
                                ),
                                DynamicShader.verticalGradient(
                                    arrayOf(
                                        Color.Transparent,
                                        Color.Black
                                    )
                                ),
                                PorterDuff.Mode.DST_IN,
                            ),
                        ),
                    )
                )
            ),
            startAxis =
            rememberStartAxis(
                label =
                rememberAxisLabelComponent(
                    color = MaterialTheme.colorScheme.onBackground,
                    background =
                    rememberShapeComponent(
                        shape = Shape.Pill,
                        color = Color.Transparent,
                        strokeColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeWidth = 1.dp,
                    ),
                    padding = Dimensions.of(horizontal = 6.dp, vertical = 2.dp),
                    margins = Dimensions.of(end = 8.dp),
                ),
                axis = null,
                tick = null,
                guideline =
                rememberLineComponent(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape =
                    remember {
                        Shape.dashed(
                            shape = Shape.Pill,
                            dashLength = 4.dp,
                            gapLength = 8.dp
                        )
                    },
                ),
                itemPlacer = remember { AxisItemPlacer.Vertical.count(count = { ITEM_PLACER_COUNT }) },
            ),
            bottomAxis =
            rememberBottomAxis(
                valueFormatter = bottomAxisValueFormatter,
                itemPlacer =
                remember {
                    AxisItemPlacer.Horizontal.default(spacing = 1, addExtremeLabelPadding = true)
                },
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = marker,
        runInitialAnimation = false,
        horizontalLayout = HorizontalLayout.fullWidth(),
        scrollState = rememberVicoScrollState(true, Scroll.Absolute.End),
    )
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = ThermometerDashboardViewModel.State(thermometerData = ThermometerData()),
            onSettingsClick = {},
            toLocalDate = { emptyMap() },
            onBackClicked = {},
            updateUnit = {}
        )
    }
}
