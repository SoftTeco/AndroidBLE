package com.softteco.template.ui.feature.deviceDashboard.devices.thermometer

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.softteco.template.R
import com.softteco.template.data.deviceData.ThermometerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random

private const val MIN_TEMPERATURE = -20
private const val MIN_HUMIDITY = 10
private const val MAX_TEMPERATURE = 45
private const val MAX_HUMIDITY = 99
const val NUM_HISTORY_ELEMENTS = 500
val xToDateMapKeyLocalDateTime = ExtraStore.Key<Map<Float, LocalDateTime>>()
val xToDateMapKeyLocalDate = ExtraStore.Key<Map<Float, LocalDate>>()
const val ITEM_PLACER_COUNT = 4

@HiltViewModel
class ThermometerDashboardViewModel @Inject constructor() : ViewModel() {

    private val thermometerData = MutableStateFlow(
        ThermometerData(
            "111",
            "Main thermometer",
            generateRandomTemperature(),
            generateRandomHumidity(),
            generateRandomThermometerHistory(NUM_HISTORY_ELEMENTS, ChronoUnit.MINUTES)
        )
    )

    private val bottomAxisValueFormatter = MutableStateFlow(
        CartesianValueFormatter { x, chartValues, _ ->
            (
                chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x] ?: LocalDateTime.ofEpochSecond(
                    x.toLong(),
                    0,
                    ZoneOffset.UTC
                )
                )
                .format(TimeIntervalMenu.Minute.dateTimeFormatter)
        }
    )

    val state = combine(
        thermometerData,
        bottomAxisValueFormatter,
    ) { data, formatter ->
        State(
            thermometerData = data,
            bottomAxisValueFormatter = formatter,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        State()
    )

    private fun generateRandomTemperature() = Random.nextInt(MIN_TEMPERATURE, MAX_TEMPERATURE).toFloat()

    private fun generateRandomHumidity() = Random.nextInt(MIN_HUMIDITY, MAX_HUMIDITY)

    private fun generateRandomThermometerHistory(numElements: Int, unit: ChronoUnit): Map<LocalDateTime, Float> {
        val startDateTime = LocalDateTime.now().minus(numElements.toLong() - 1, unit)
        return (0 until numElements).associate { i ->
            startDateTime.plus(i.toLong(), unit) to generateRandomTemperature()
        }
    }

    fun toLocalDate(values: Map<LocalDateTime, Float>): Map<LocalDate, Float> {
        return values.entries
            .groupBy { it.key.toLocalDate() }
            .mapValues { (_, entries) ->
                entries.map { it.value }.average().toFloat()
            }
    }

    fun updateThermometerHistory(numElements: Int, unit: TimeIntervalMenu) {
        val newTemperatureHistory = generateRandomThermometerHistory(numElements, unit.chronoUnit)

        val updatedThermometerData = thermometerData.value.copy(
            temperatureHistory = newTemperatureHistory
        )

        if (unit == TimeIntervalMenu.Minute || unit == TimeIntervalMenu.Hour) {
            bottomAxisValueFormatter.value = CartesianValueFormatter { x, chartValues, _ ->
                (
                    chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x] ?: LocalDateTime.ofEpochSecond(
                        x.toLong(),
                        0,
                        ZoneOffset.UTC
                    )
                    )
                    .format(unit.dateTimeFormatter)
            }
        } else {
            bottomAxisValueFormatter.value = CartesianValueFormatter { x, chartValues, _ ->
                (chartValues.model.extraStore[xToDateMapKeyLocalDate][x] ?: LocalDate.ofEpochDay(x.toLong()))
                    .format(unit.dateTimeFormatter)
            }
        }

        thermometerData.value = updatedThermometerData
    }
    fun updateCurrentTemperature() {
        val newCurrentTemperature = generateRandomTemperature()

        val updatedThermometerData = thermometerData.value.copy(
            currentTemperature = newCurrentTemperature
        )

        thermometerData.value = updatedThermometerData
    }

    fun updateCurrentHumidity() {
        val newCurrentHumidity = generateRandomHumidity()

        val updatedThermometerData = thermometerData.value.copy(
            currentHumidity = newCurrentHumidity
        )

        thermometerData.value = updatedThermometerData
    }

    @Immutable
    data class State(
        val thermometerData: ThermometerData = ThermometerData(),
        val bottomAxisValueFormatter: CartesianValueFormatter = CartesianValueFormatter { x, chartValues, _ ->
            (
                chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x] ?: LocalDateTime.ofEpochSecond(
                    x.toLong(),
                    0,
                    ZoneOffset.UTC
                )
                )
                .format(TimeIntervalMenu.Day.dateTimeFormatter)
        }
    )

    enum class TimeIntervalMenu(
        @StringRes val labelResourceID: Int,
        val dateTimeFormatter: DateTimeFormatter,
        val chronoUnit: ChronoUnit
    ) {
        Minute(R.string.minute, DateTimeFormatter.ofPattern("d MMM, HH:mm"), ChronoUnit.MINUTES),
        Hour(R.string.hour, DateTimeFormatter.ofPattern("d MMM, HH"), ChronoUnit.HOURS),
        Day(R.string.day, DateTimeFormatter.ofPattern("d MMM yy"), ChronoUnit.DAYS),
        Month(R.string.month, DateTimeFormatter.ofPattern("MMM yy"), ChronoUnit.MONTHS)
    }
}
