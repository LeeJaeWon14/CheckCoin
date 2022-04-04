package com.example.checkcoin.view.candlestick

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.checkcoin.R
import com.example.checkcoin.databinding.CandleStickLayoutBinding
import com.example.checkcoin.model.dto.CandleStickDTO
import com.example.checkcoin.model.service.CheckService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class CandleStickFragment : Fragment() {
    private var _binding: CandleStickLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var act : Activity
    private lateinit var title : String

    companion object {
        //Fragment Instance 생성
        fun newInstance(page : Int, title : String) : CandleStickFragment {
            val fragment : CandleStickFragment = CandleStickFragment()
            val args : Bundle = Bundle()
            args.putInt("page", page)
            args.putString("title", title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        //상위 Activity의 Context 가져옴
        super.onAttach(context)
        if(context is Activity) { act = context }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString("title").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CandleStickLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        //Retrofit 초기화
        val retrofit : Retrofit = Retrofit.Builder()
            .baseUrl("https://api.bithumb.com/public/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service : CheckService = retrofit.create(CheckService::class.java)
        val candle = service.getCandleStick(title, "krw", "1m")

        CoroutineScope(Dispatchers.IO).launch {
            candle.enqueue(object : Callback<CandleStickDTO> {
                override fun onResponse(call: Call<CandleStickDTO>, response: Response<CandleStickDTO>) {
                    if(response.isSuccessful) {
                        val result = response.body()!!

                        setChart(result.data!!)
                    }
                    else {
                        act.runOnUiThread(Runnable { Toast.makeText(act, "Response 실패", Toast.LENGTH_SHORT).show() })
                    }
                }

                override fun onFailure(call: Call<CandleStickDTO>, t: Throwable) {
                    act.runOnUiThread(Runnable { Toast.makeText(act, "통신 실패", Toast.LENGTH_SHORT).show() })
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    //그래프 차트 설정
    private fun setChart(records : List<List<String>>) {
        val lineChart : LineChart = binding.chart
        lineChart.invalidate()
        lineChart.clear()

        val dateList : ArrayList<Long> = ArrayList<Long>()
        val entries : ArrayList<Entry> = ArrayList<Entry>()

        for(i in records.size-1 downTo records.size-10) {
            entries.add(Entry((records.size-i).toFloat(), records.get(i).get(1).toFloat()))
            dateList.add(records.get(i).get(0).toLong())
        }

        val lineDataSet : LineDataSet = LineDataSet(entries, "Price")
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        lineDataSet.circleHoleColor = ContextCompat.getColor(requireContext(), R.color.white)
        lineDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.white))

        val lineData : LineData = LineData()
        lineData.addDataSet(lineDataSet)

        lineData.setValueTextColor(Color.WHITE)
        lineData.setValueTextSize(9f)

        //X축 설정
        val xAxis : XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP
        xAxis.setLabelCount(10, true)
        xAxis.textColor = Color.WHITE
        xAxis.gridColor = Color.WHITE
        xAxis.valueFormatter = AxisValueFormatter(lineChart, dateList)

        //Y축 왼쪽 설정
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawLabels(false)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.setDrawGridLines(false)

        //Y축 오른쪽 설정
        val yAxisRight = lineChart.axisRight
        yAxisRight.textColor = Color.WHITE
        yAxisRight.gridColor = Color.WHITE

        //Chart 속성 설정
        lineChart.description = null
        lineChart.legend.textColor = Color.WHITE
        lineChart.data = lineData
        lineChart.setNoDataText("wait..")
    }

    //축 단위 설정하는 클래스
    inner class AxisValueFormatter(chart : LineChart, dateList : ArrayList<Long>) : ValueFormatter() {
        lateinit var chart : LineChart
        lateinit var dateList : ArrayList<Long>
        init {
            this.chart = chart
            this.dateList = dateList
        }
        override fun getFormattedValue(value: Float): String {
            val date : Date = Date(dateList.get(value.toInt() -1))
            val dateFormat : String = SimpleDateFormat("hh:mm", Locale.getDefault()).format(date)

            return dateFormat
        }
    }
}