package com.neil.reportph.models

import androidx.annotation.Keep
import com.neil.reportph.Constants
import java.io.Serializable

@Keep
data class Reports(val latitude: Double = -1.0,
                   val longtitude: Double = -1.0,
                   val crime: String = Constants.STRING_EMPTY,
                   val description: String = Constants.STRING_EMPTY,
                   val time: String = Constants.STRING_EMPTY,
                   val date: String = Constants.STRING_EMPTY): Serializable