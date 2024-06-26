package com.softteco.template.ui.feature.deviceDashboard.devices.thermometer

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.softteco.template.R
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.usecase.ThermometerDataGetUseCase
import com.softteco.template.data.deviceData.ThermometerData
import com.softteco.template.data.deviceData.ThermometerValues
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

val xToDateMapKeyLocalDateTime = ExtraStore.Key<Map<Float, LocalDateTime>>()
val xToDateMapKeyLocalDate = ExtraStore.Key<Map<Float, LocalDate>>()
const val ITEM_PLACER_COUNT = 4

@HiltViewModel
class ThermometerDashboardViewModel @Inject constructor(
    private val bluetoothHelper: BluetoothHelper,
    private val thermometerDataGetUseCase: ThermometerDataGetUseCase,
) :
    ViewModel() {

    private val thermometerData = MutableStateFlow(
        ThermometerData()
    )

    var unit = TimeIntervalMenu.Minute

    private val bottomAxisValueFormatter = MutableStateFlow(
        CartesianValueFormatter { x, chartValues, _ ->
            (
                    chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
                        ?: LocalDateTime.ofEpochSecond(
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
        bottomAxisValueFormatter
    ) { data, formatter ->
        State(
            thermometerData = data,
            bottomAxisValueFormatter = formatter
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        State()
    )

    fun toLocalDate(values: List<ThermometerValues>): Map<LocalDate, Double> {
        return values
            .groupBy { (it as ThermometerValues.DataLYWSD03MMC).timestamp.toLocalDate() }
            .mapValues { (_, entries) ->
                entries.map { (it as ThermometerValues.DataLYWSD03MMC).temperature }.average()
            }
    }

    fun onDeviceResultCallback(onDeviceResult: (macAddress: String) -> Unit) {
        bluetoothHelper.onDeviceResultCallback(onDeviceResult)
    }

    fun getThermometerValues(macAddress: String) {
        thermometerData.value =
            thermometerDataGetUseCase.execute(macAddress).let { thermometerDataDb ->
                thermometerDataDb.toEntity().let { thermometerData ->
                    val lastElement =
                        thermometerData.valuesHistory.groupBy { (it as ThermometerValues.DataLYWSD03MMC).timestamp }.values.map {
                            it.map { it as ThermometerValues.DataLYWSD03MMC }
                                .maxBy(ThermometerValues.DataLYWSD03MMC::timestamp)
                        }.last()
                    thermometerData.currentTemperature = lastElement.temperature
                    thermometerData.currentHumidity = lastElement.humidity
                    thermometerData
                }
            }.also {
                updateThermometerHistory() }
    }

    private fun updateThermometerHistory() {
        if (unit == TimeIntervalMenu.Minute || unit == TimeIntervalMenu.Hour) {
            bottomAxisValueFormatter.value = CartesianValueFormatter { x, chartValues, _ ->
                (
                        chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
                            ?: LocalDateTime.ofEpochSecond(
                                x.toLong(),
                                0,
                                ZoneOffset.UTC
                            )
                        )
                    .format(unit.dateTimeFormatter)
            }
        } else {
            bottomAxisValueFormatter.value = CartesianValueFormatter { x, chartValues, _ ->
                (chartValues.model.extraStore[xToDateMapKeyLocalDate][x]
                    ?: LocalDate.ofEpochDay(x.toLong()))
                    .format(unit.dateTimeFormatter)
            }
        }
    }

    @Immutable
    data class State(
        val thermometerData: ThermometerData = ThermometerData(),
        val bottomAxisValueFormatter: CartesianValueFormatter = CartesianValueFormatter { x, chartValues, _ ->
            (
                    chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
                        ?: LocalDateTime.ofEpochSecond(
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
