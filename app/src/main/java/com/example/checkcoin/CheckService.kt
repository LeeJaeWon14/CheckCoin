package com.example.checkcoin

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CheckService {
    @GET("ticker/{order}_{payment}")
    fun getTicker(@Path("order") order : String, @Path("payment") payment : String) : Call<TickerDTO>

    @GET("candlestick/{order}_{payment}/{interval}")
    fun getCandleStick(@Path("order") order : String, @Path("payment") payment : String, @Path("interval") interval : String) : Call<CandleStickDTO>
}