package it.polito.timebankingapp

import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    var count = 0
    lateinit var tv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val b = findViewById<Button>(R.id.button)
        tv = findViewById<TextView>(R.id.textView)

        b.setOnClickListener{
            count++
            tv.text = "$count"
            if (count % 5 == 0){
                val i = Intent(this, CongratulationsActivity::class.java)
                i.putExtra("threshold", count)
                startActivity(i)
                //we want the message receives something from here
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("count", count)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        count = savedInstanceState.getInt("count",0)
        tv.text = "$count"

    }

}