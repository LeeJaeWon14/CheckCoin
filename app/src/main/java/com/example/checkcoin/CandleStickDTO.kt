package com.example.checkcoin

import com.google.gson.annotations.SerializedName

class CandleStickDTO {
    @SerializedName("status")
    var status : String = ""

    @SerializedName("data")
    var data : List<List<String>>? = null
}