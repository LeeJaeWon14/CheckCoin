package com.example.checkcoin

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.checkcoin.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val viewModel : ViewModel by lazy { ViewModel() }
    val listItems : Array<String> = arrayOf("비트코인(BTC)", "이더리움(ETH)", "스팀코인(STEEM)", "에이다(ADA)", "비트코인 캐시(bch)", "직접 입력")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding : ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel

        viewPager.visibility = View.INVISIBLE

        selectCoin.setOnClickListener {
            val dlg = AlertDialog.Builder(this@MainActivity)
                .setItems(listItems, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
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
                                    val sendIntent = Intent(Intent.ACTION_VIEW)
                                        .setData(Uri.parse("https://www.bithumb.com/"))
                                    val chooser : Intent = Intent.createChooser(sendIntent, "브라우저 선택")
                                    sendIntent.resolveActivity(packageManager).let {
                                        startActivity(chooser)
                                    }
                                }

                                addDlg.show()
                            }
                        }
                    }
                })

            dlg.show()
        }

        getButton.setOnClickListener {
            val selectCoinText : String
            if(selectCoin.text.toString().contains("(")) {
                selectCoinText = selectCoin.text.toString().split("(")[1].split(")")[0]
                Toast.makeText(this@MainActivity, selectCoinText, Toast.LENGTH_SHORT).show()
                runGetTicker(selectCoinText)
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

    inner class ViewModel {
        var searchKeyword = ObservableField<String>()

    }

    class MyPagerAdapter(fragmentManager : FragmentManager, title : String) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val title : String = title
        companion object {
            var NUM_ITEMS : Int = 2;
        }
        override fun getItem(position: Int): Fragment {
            when(position) {
                0 -> { return CandleStickFragment.newInstance(0, title) }
                1 -> { return TickerFragment.newInstance(1, title) }
            }
            return Fragment()
        }

        override fun getCount(): Int {
            return NUM_ITEMS
        }
    }

    private fun runGetTicker(coin : String) {
        viewPager.visibility = View.VISIBLE
        noSelectText.visibility = View.INVISIBLE

        println("now >> ${coin}")

        if(viewPager.isActivated) {
            println("now >> ViewPager Activated")
            viewPager.removeAllViews()
        }
        val viewPAgerAdapter = MyPagerAdapter(supportFragmentManager, coin)
        viewPager.adapter = viewPAgerAdapter
    }
}