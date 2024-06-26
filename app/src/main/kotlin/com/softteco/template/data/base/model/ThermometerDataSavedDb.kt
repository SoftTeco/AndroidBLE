package com.softteco.template.data.base.model

import com.softteco.template.data.deviceData.ThermometerData

abstract class ThermometerDataSavedDb {
    abstract fun toEntity(): ThermometerData
}
