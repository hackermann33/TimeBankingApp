package it.polito.timebankingapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class User( var userId: Int, var picPath:String, var fullName: String, var nick: String, var email: String,
            var location: String, var skills: List<String>) {

} {/**/}

class ShowProfileActivity : AppCompatActivity() {
    var picPath = "/sdcard/test2.png" /* insert a real image path -> image should be in drawable */
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
    lateinit var picView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_showprofileactivity)

        picView = findViewById(R.id.test_image)
        val bMap: Bitmap = BitmapFactory.decodeFile(picPath)
        picView.setImageBitmap(bMap);

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
        //editProfile() //evoked when the pencil button is pressed
    }

    private fun editProfile() {
        //to-do explicit intent targeting the editprofileactivity class
        //intent.putExtra("group13.lab.VAR_NAME", var_name)

        //startActivityForResult(intent, ....)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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