package com.softteco.template.data.deviceData

import java.time.LocalDateTime

data class ThermometerData(
    val deviceId: String = "",
    val deviceName: String = "",
    val currentTemperature: Float = 0.0f,
    val currentHumidity: Int = 0,
    val temperatureHistory: Map<LocalDateTime, Float> = emptyMap()
)
