package com.example.checkcoin.view.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.checkcoin.R
import com.example.checkcoin.databinding.ActivityMainBinding
import com.example.checkcoin.util.Log
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import okhttp3.Dispatcher

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val listItems : Array<String> = arrayOf("비트코인(BTC)", "이더리움(ETH)", "스팀코인(STEEM)", "에이다(ADA)", "비트코인 캐시(bch)", "직접 입력")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        binding.apply {
            //첫 화면에서 ViewPager 가림
            viewPager.visibility = View.INVISIBLE

            selectCoin.setOnClickListener {
                val dlg = AlertDialog.Builder(this@MainActivity)
                    .setItems(listItems, coinClickListener)

                dlg.show()
            }

            getButton.setOnClickListener {
                val selectCoinText : String
                if(selectCoin.text.toString().contains("(")) {
                    selectCoinText = selectCoin.text.toString().split("(")[1].split(")")[0]
                    Snackbar.make(it, "새로고침 되었습니다.", Snackbar.LENGTH_SHORT).show()
                    runGetTicker(selectCoinText)
                }
            }
        }
    }

    private var time : Long = 0
    override fun onBackPressed() {
        if(System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis()
            Toast.makeText(this@MainActivity, "한번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show()
        }
        else if(System.currentTimeMillis() - time < 2000) {
            this.finishAffinity()
        }
    }

    //Ticker api call
    private fun runGetTicker(coin : String) {
        binding.apply {
            viewPager.visibility = View.VISIBLE
            noSelectText.visibility = View.INVISIBLE

            //ViewPager 지웠다가 초기화
            if(viewPager.isActivated) {
                Log.e("now >> ViewPager Activated")
                viewPager.removeAllViews()
            }
            viewPager.adapter = MyPagerAdapter(supportFragmentManager, coin)
            startTimer()
        }
    }

    private val coinClickListener = DialogInterface.OnClickListener { dialog, which ->
        binding.apply {
            when(which) {
                0 -> {
                    selectCoin.setText(listItems[which])
                    runGetTicker("btc")
                }
                1 -> {
                    selectCoin.setText(listItems[which])
                    runGetTicker("eth")
                }
                2 -> {
                    selectCoin.setText(listItems[which])
                    runGetTicker("steem")
                }
                3 -> {
                    selectCoin.setText(listItems[which])
                    runGetTicker("ada")
                }
                4 -> {
                    selectCoin.setText(listItems[which])
                    runGetTicker("bch")
                }
                5 -> {
                    val dlgView = View.inflate(this@MainActivity, R.layout.add_coin_layout, null)
                    val builder = AlertDialog.Builder(this@MainActivity)
                    val addDlg = builder.create()

                    addDlg.setView(dlgView)
                    addDlg.window?.setBackgroundDrawableResource(R.drawable.block)

                    val addCoinButton = dlgView.findViewById<Button>(R.id.addCoinButton)
                    val addCoin = dlgView.findViewById<EditText>(R.id.addCoin)
                    val coinList = dlgView.findViewById<TextView>(R.id.coinList)

                    addCoinButton.setOnClickListener {
                        selectCoin.setText(addCoin.text.toString())
                        runGetTicker(addCoin.text.toString())
                        addDlg.dismiss()
                    }

                    coinList.setOnClickListener {
                        //Replaced webView
                        /*val sendIntent = Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse("https://www.bithumb.com/"))
                        val chooser : Intent = Intent.createChooser(sendIntent, "브라우저 선택")
                        sendIntent.resolveActivity(packageManager).let {
                            startActivity(chooser)
                        }*/

                        val webViewLayout = View.inflate(this@MainActivity, R.layout.web_layout, null)
                        val webViewDialog = AlertDialog.Builder(this@MainActivity).create()
                        webViewDialog.setView(webViewLayout)
                        webViewDialog.window?.setBackgroundDrawableResource(R.drawable.block)

                        val webView = webViewLayout.findViewById<WebView>(R.id.coinWebView)
                        val webViewButton = webViewLayout.findViewById<Button>(R.id.webViewButton)

                        webView.apply {
                            webViewClient = WebViewClient()
                            with(settings) {
                                //javaScriptEnabled = true
                                loadWithOverviewMode = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                builtInZoomControls = true
                                setSupportZoom(true)
                            }
                        }
                        webView.loadUrl("https://www.bithumb.com/")
                        webViewButton.setOnClickListener { webViewDialog.dismiss() }

                        webViewDialog.show()
                    }

                    addDlg.show()
                }
            }
        }
    }

    private fun startTimer() {
        var count = 0
        CoroutineScope(Dispatchers.Default).launch {
            while(true) {
                delay(1000)
                count ++
                Log.e("count tik-tok! $count")
                if(count == 10) {
                    withContext(Dispatchers.Main) {
                        binding.getButton.performClick()
                    }
                    break
                }
            }
        }
    }
}