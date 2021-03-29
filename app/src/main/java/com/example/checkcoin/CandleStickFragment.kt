package com.example.checkcoin

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.candle_stick_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CandleStickFragment : Fragment() {
    private lateinit var act : Activity
    private lateinit var title : String
    companion object {
        fun newInstance(page : Int, title : String, type : String = "time") : CandleStickFragment {
            val fragment : CandleStickFragment = CandleStickFragment()
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
        title = arguments?.getString("title").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.candle_stick_layout, container, false)
        val chart = view.findViewById<LineChart>(R.id.chart)
        return view
    }

    override fun onStart() {
        super.onStart()

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

    private fun setChart(records : List<List<String>>) {
        val lineChart : LineChart = chart
        lineChart.invalidate()
        lineChart.clear()

        val dateList : ArrayList<Long> = ArrayList<Long>()

        val entries : ArrayList<Entry> = ArrayList<Entry>()

        for(i in records.size-1 downTo records.size-10) {
            entries.add(Entry((records.size-i).toFloat(), records.get(i).get(1).toFloat()))
            dateList.add(records.get(i).get(0).toLong())
        }


        val lineDataSet : LineDataSet = LineDataSet(entries, "Price")
        lineDataSet.setColor(resources.getColor(R.color.colorPrimary))
        lineDataSet.circleHoleColor = resources.getColor(R.color.white)
        lineDataSet.setCircleColor(resources.getColor(R.color.white))

        val lineData : LineData = LineData()
        lineData.addDataSet(lineDataSet)

        lineData.setValueTextColor(Color.WHITE)
        lineData.setValueTextSize(9f)

        val xAxis : XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP
        xAxis.setLabelCount(10, true)
        xAxis.textColor = Color.WHITE
        xAxis.gridColor = Color.WHITE
        xAxis.valueFormatter = AxisValueFormatter(lineChart, dateList)

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawLabels(false)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.setDrawGridLines(false)

        val yAxisRight = lineChart.axisRight
        yAxisRight.textColor = Color.WHITE
        yAxisRight.gridColor = Color.WHITE


        //lineChart.setVisibleXRangeMinimum((60 * 60 * 24 * 1000 * 5).toFloat())
        lineChart.description = null
        lineChart.legend.textColor = Color.WHITE
        lineChart.data = lineData
    }

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