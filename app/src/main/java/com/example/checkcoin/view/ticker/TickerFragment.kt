package com.example.checkcoin.view.ticker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
            Log.e("instance")
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
        _binding = TickerLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        title = arguments?.getString("title").toString()
        Log.e("coin >> ${title}")

        //Retrofit 초기화
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
                        initText(result!!)
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

    fun initText(result : TickerDTO) {
        val date : Date = Date(result?.data?.date!!.toLong())
        val dateFormat : String = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(date)

        binding.apply {
            tickerOpening.text = tickerOpening.text.toString() + "\r\n ${makeComma(result?.data?.openingPrice!!)}원" //시가
            tickerClosing.text = tickerClosing.text.toString() + "\r\n ${makeComma(result?.data?.closingPrice!!)}원" //종가
            tickerMin.text = tickerMin.text.toString() + "\r\n ${makeComma(result?.data?.minPrice!!)}원" //저가
            tickerMax.text = tickerMax.text.toString() + "\r\n ${makeComma(result?.data?.maxPrice!!)}원" //고가
            tickerUnitsTraded.text = tickerUnitsTraded.text.toString() + "\r\n ${makeComma(cutString(result?.data?.unitsTraded!!))}건" //거래량
            tickerAccTradeValue.text = tickerAccTradeValue.text.toString() + "\r\n ${makeComma(cutString(result?.data?.accTradeValue!!))}원" //거래금액
            tickerPrevClosing.text = tickerPrevClosing.text.toString() + "\r\n ${makeComma(result?.data?.prevClosingPrice!!)}원" //전일종가
            tickerUnitsTraded24H.text = tickerUnitsTraded24H.text.toString() + "\r\n ${makeComma(cutString(result?.data?.unitsTraded_24H!!))}건" //24시간 거래량
            tickerFluctate24H.text = tickerFluctate24H.text.toString() + "\r\n ${makeComma(cutString(result?.data?.fluctate_24H!!))}원" //24시간 변동가
            tickerFluctateRate24H.text = tickerFluctateRate24H.text.toString() + "\r\n ${result?.data?.fluctateRate_24H}%" //24시간 변동률
            tickerDate.text = "${dateFormat} ${tickerDate.text.toString()}" //기준 시간
        }
    }

    fun makeComma(price : String) : String {
        if(price.contains(".") || price.length < 4) {
            return price
        }
        val formatter = DecimalFormat("###,###")
        return formatter.format(price.toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("fragment destroy")
        _binding = null
        job.cancel()
    }
}