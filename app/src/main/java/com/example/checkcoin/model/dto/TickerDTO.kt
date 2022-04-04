package com.example.checkcoin.model.dto

import com.google.gson.annotations.SerializedName

class TickerDTO {
    @SerializedName("status")
    var status : String = ""

    @SerializedName("data")
    var data : TickerDataDTO? = null
}