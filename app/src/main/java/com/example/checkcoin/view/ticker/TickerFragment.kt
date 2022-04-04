package com.example.checkcoin.view.ticker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.checkcoin.R
import com.example.checkcoin.databinding.TickerLayoutBinding
import com.example.checkcoin.model.dto.TickerDTO
import com.example.checkcoin.model.service.CheckService
import com.example.checkcoin.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TickerFragment : Fragment() {
    private var _binding: TickerLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var act : Activity
    private lateinit var title : String
    private lateinit var job : Job
    var result : TickerDTO? = null

    companion object {
        //Fragment Instance 생성
        fun newInstance(page : Int, title : String) : TickerFragment {
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
        Log.e("Fragment Start")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("ticker onCreateView")
        _binding = TickerLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        title = arguments?.getString("title").toString()

        //Retrofit 초기화
        val retrofit : Retrofit = Retrofit.Builder().run {
            baseUrl("https://api.bithumb.com/public/")
            addConverterFactory(GsonConverterFactory.create())
            build()
        }
        val service : CheckService = retrofit.create(CheckService::class.java)
        val ticker = service.getTicker(title, "krw")

        job = CoroutineScope(Dispatchers.IO).launch {
            ticker.enqueue(object : Callback<TickerDTO> {
                override fun onResponse(call: Call<TickerDTO>, response: Response<TickerDTO>) {
                    if(response.isSuccessful) {
                        response.body()?.let {
                            Log.e("response is success")
                            initText(it)
                        }
                    }
                    else {
                        act.runOnUiThread(Runnable { Toast.makeText(act, getString(R.string.str_response_failure), Toast.LENGTH_SHORT).show() })
                    }
                }

                override fun onFailure(call: Call<TickerDTO>, t: Throwable) {
                    act.runOnUiThread(Runnable { Toast.makeText(act, String.format(getString(R.string.str_response_failure), t.toString()), Toast.LENGTH_SHORT).show() })
                }

            })
        }
        job.start()
    }

    private fun cutString(str : String) : String{
        return str.split(".").get(0)
    }

    fun initText(result : TickerDTO) {
        val date : Date = Date(result.data?.date!!.toLong())

        binding.apply {
            result.data?.let {
                tickerOpening.text = String.format(getString(R.string.str_ticker_opening), makeComma(it.openingPrice)) //시가
                tickerClosing.text = String.format(getString(R.string.str_ticker_closing), makeComma(it.closingPrice)) //종가
                tickerMin.text = String.format(getString(R.string.str_ticker_min), makeComma(it.minPrice)) //저가
                tickerMax.text = String.format(getString(R.string.str_ticker_max), makeComma(it.maxPrice)) //고가
                tickerUnitsTraded.text = String.format(getString(R.string.str_ticker_units_traded), makeComma(cutString(it.unitsTraded))) //거래량
                tickerAccTradeValue.text = String.format(getString(R.string.str_ticker_acc_trade_value), makeComma(cutString(it.accTradeValue))) //거래금액
                tickerPrevClosing.text = String.format(getString(R.string.str_ticker_prev_closing), makeComma(it.prevClosingPrice)) //전일종가
                tickerUnitsTraded24H.text = String.format(getString(R.string.str_ticker_units_traded_24h), makeComma(cutString(it.unitsTraded_24H))) //24시간 거래량
                tickerFluctate24H.text = String.format(getString(R.string.str_ticker_fluctate_24h), makeComma(cutString(it.fluctate_24H))) //24시간 변동가
                tickerFluctateRate24H.text = String.format(getString(R.string.str_ticker_fluctate_rate_24h), it.fluctateRate_24H).plus("%") //24시간 변동률
                tickerDate.text = String.format(getString(R.string.str_ticker_data), dateFormat(date)) //기준 시간
            }
        }
    }

    private fun makeComma(price : String) : String {
        if(price.contains(".") || price.length < 4) {
            return price
        }
        val formatter = DecimalFormat("###,###")
        return formatter.format(price.toLong())
    }

    private fun dateFormat(date: Date) : String = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(date)

    override fun onDestroy() {
        super.onDestroy()
        Log.e("fragment destroy")
        _binding = null
        job.cancel()
    }
}