package com.example.checkcoin

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ticker_layout.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*

class TickerFragment : Fragment() {
    private lateinit var act : Activity
    private lateinit var title : String
    private lateinit var job : Job
    var result : TickerDTO? = null
    companion object {
        fun newInstance(page : Int, title : String) : TickerFragment {
            println("instance")
            val fragment : TickerFragment = TickerFragment()
            val args : Bundle = Bundle()
            args.putInt("page", page)
            args.putString("title", title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is Activity) { act = context }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Fragment Start")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ticker_layout, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()

        title = arguments?.getString("title").toString()
        println("coin >> ${title}")

        val retrofit : Retrofit = Retrofit.Builder()
            .baseUrl("https://api.bithumb.com/public/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service : CheckService = retrofit.create(CheckService::class.java)
        val ticker = service.getTicker(title, "krw")

        job = CoroutineScope(Dispatchers.IO).launch {
            ticker.enqueue(object : Callback<TickerDTO> {
                override fun onResponse(call: Call<TickerDTO>, response: Response<TickerDTO>) {
                    if(response.isSuccessful) {
                        result = response.body()

                        val date : Date = Date(result?.data?.date!!.toLong())
                        val dateFormat : String = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(date)

                        tickerOpening.text = tickerOpening.text.toString() + "\r\n ${result?.data?.openingPrice}원"
                        tickerClosing.text = tickerClosing.text.toString() + "\r\n ${result?.data?.closingPrice}원"
                        tickerMin.text = tickerMin.text.toString() + "\r\n ${result?.data?.minPrice}원"
                        tickerMax.text = tickerMax.text.toString() + "\r\n ${result?.data?.maxPrice}원"
                        tickerUnitsTraded.text = tickerUnitsTraded.text.toString() + "\r\n ${cutString(result?.data?.unitsTraded!!)}건"
                        tickerAcc_trade_value.text = tickerAcc_trade_value.text.toString() + "\r\n ${cutString(result?.data?.accTradeValue!!)}원"
                        tickerPrevClosing.text = tickerPrevClosing.text.toString() + "\r\n ${result?.data?.prevClosingPrice}원"
                        tickerUnitsTraded_24H.text = tickerUnitsTraded_24H.text.toString() + "\r\n ${cutString(result?.data?.unitsTraded_24H!!)}건"
                        tickerFluctate_24H.text = tickerFluctate_24H.text.toString() + "\r\n ${cutString(result?.data?.fluctate_24H!!)}원"
                        tickerFluctateRate_24H.text = tickerFluctateRate_24H.text.toString() + "\r\n ${result?.data?.fluctateRate_24H}%"
                        tickerDate.text = "${dateFormat} ${tickerDate.text.toString()}"
                    }
                    else {
                        act.runOnUiThread(Runnable { Toast.makeText(act, "Respose 실패", Toast.LENGTH_SHORT).show() })
                    }
                }

                override fun onFailure(call: Call<TickerDTO>, t: Throwable) {
                    act.runOnUiThread(Runnable { Toast.makeText(act, "통신 실패 : ${t.message}", Toast.LENGTH_SHORT).show() })
                }

            })
        }
        job.start()
    }

    fun cutString(str : String) : String{
        return str.split(".").get(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("fragment destroy")
        job.cancel()
    }
}