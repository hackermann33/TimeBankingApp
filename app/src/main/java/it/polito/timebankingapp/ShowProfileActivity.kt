package it.polito.timebankingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast

class ShowProfileActivity : AppCompatActivity() {
    var count = 0
    var fullName = "Name Surname"
    var nick = "example"
    var email = "example@test.com"
    var location = "45.070312, 7.6868565"
    var skills: List<String> = mutableListOf()
    var balance = 3

    lateinit var nameView: TextView
    lateinit var nickView: TextView
    lateinit var emailView: TextView
    lateinit var locationView: TextView
    lateinit var balanceView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_showprofileactivity)
        nameView = findViewById<TextView>(R.id.fullName)
        nameView.text = fullName

        nickView = findViewById(R.id.nickname)
        nickView.text = nick

        emailView = findViewById<TextView>(R.id.email)
        emailView.text = email

        locationView = findViewById<TextView>(R.id.location)
        locationView.text = location

        balanceView = findViewById<TextView>(R.id.balance)
        balanceView.text = "$balance"

        /*b.setOnClickListener{
            count++
            tv.text = "$count"
            if (count % 5 == 0){
                val i = Intent(this, CongratulationsActivity::class.java)
                i.putExtra("threshold", count)
                startActivity(i)
                //we want the message receives something from here
            }
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_showprofileactivity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                Toast.makeText(
                    applicationContext, "Edit profile",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
       *//* super.onSaveInstanceState(outState)
        outState.putInt("count", count)*//*
    }*/

    /*override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        *//*super.onRestoreInstanceState(savedInstanceState)
        count = savedInstanceState.getInt("count",0)
        tv.text = "$count"*//*

    }*/

}