package com.softteco.template.data.base.model

import com.softteco.template.data.deviceData.ThermometerValues

abstract class ThermometerValuesSavedDb {
    abstract fun toEntity(): ThermometerValues
}
