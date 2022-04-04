package com.example.checkcoin.model.dto

import com.google.gson.annotations.SerializedName

class TickerDataDTO {
    @SerializedName("opening_price")
    var openingPrice : String = ""

    @SerializedName("closing_price")
    var closingPrice : String = ""

    @SerializedName("min_price")
    var minPrice : String = ""

    @SerializedName("max_price")
    var maxPrice : String = ""

    @SerializedName("units_traded")
    var unitsTraded : String = ""

    @SerializedName("acc_trade_value")
    var accTradeValue : String = ""

    @SerializedName("prev_closing_price")
    var prevClosingPrice : String = ""

    @SerializedName("units_traded_24H")
    var unitsTraded_24H : String = ""

    @SerializedName("acc_trade_value_24H")
    var accTradeValue_24H : String = ""

    @SerializedName("fluctate_24H")
    var fluctate_24H : String = ""

    @SerializedName("fluctate_rate_24H")
    var fluctateRate_24H : String = ""

    @SerializedName("date")
    var date : Number = 0
}