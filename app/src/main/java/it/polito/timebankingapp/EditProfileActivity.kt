package it.polito.timebankingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofileactivity)
        val tv3 = findViewById<TextView>(R.id.textView3)
        val i = intent
        val v = i.getIntExtra("threshold", 0)
        tv3.text = "You reached $v"
    }
}