package com.softteco.template.data.deviceData

import java.util.UUID

data class ThermometerData(
    val deviceId: UUID = UUID.randomUUID(),
    val deviceName: String = "",
    val macAddress: String = "",
    var currentTemperature: Double = 0.0,
    var currentHumidity: Int = 0,
    val valuesHistory: List<ThermometerValues> = listOf()
)
