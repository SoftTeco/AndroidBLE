package com.softteco.template.ui.feature.home.device.connection.manual

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.softteco.template.data.device.SupportedDevice
import com.softteco.template.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class ManualSelectionViewModel @Inject constructor() : ViewModel() {

    private val _navDestination = MutableSharedFlow<Screen>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navDestination = _navDestination.asSharedFlow()

    private val supportedDevices = MutableStateFlow(
        listOf(
            SupportedDevice(
                com.softteco.template.data.device.Device.Type.TemperatureAndHumidity,
                com.softteco.template.data.device.Device.Family.Sensor,
                com.softteco.template.data.device.Device.Model.LYWSD03MMC,
                "file:///android_asset/icon/temperature_monitor.webp"
            )
        )
    )

    val state = MutableStateFlow(State(supportedDevices.value))

    @Immutable
    data class State(
        val devices: List<SupportedDevice> = emptyList(),
    )
}
