package com.neil.reportph.models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Reports(val latitude: Double = -1.0,
                   val longtitude: Double = -1.0,
                   val crime: String = "",
                   val description: String = "",
                   val time: String = "",
                   val date: String = ""): Serializable