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
import java.io.Serializable


class User(var pic:String, var fullName: String = "", var nick: String="", var email: String="",
           var location: String="", var skills: List<String> = emptyList(), var balance: Int = 0) : Serializable{
    fun isGood(): Boolean {
        return pic.isNotEmpty() && fullName.isNotEmpty() && nick.isNotEmpty() && email.isNotEmpty() && location.isNotEmpty()

    }
    /*{ }*/


}

class ShowProfileActivity : AppCompatActivity() {

    lateinit var usr: User
    val LAUNCH_EDIT_ACTIVITY = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        /* All these info will be retrieved from server */
        val proPic = BitmapFactory.decodeResource(baseContext.resources, R.drawable.default_avatar)
        val fullName = "Name Surname"
        val nick = "example"
        val email = "example@test.com"
        val location = "45.070312, 7.6868565"
        val skills: List<String> = mutableListOf()
        val balance = 3

        usr = User(" ", fullName, nick, email, location, skills)


        setContentView(R.layout.activity_showprofileactivity)


        /*picView = findViewById(R.id.profile_pic)
        val bMap: Bitmap = BitmapFactory.decodeFile(picPath)
        picView.setImageBitmap(bMap);*/

        displayUser(usr)

    }

    fun displayUser(usr: User){


        val nameView = findViewById<TextView>(R.id.fullName)
        nameView.text = usr.fullName

        val nickView = findViewById<TextView>(R.id.nickname)
        nickView.text = usr.nick

        val emailView = findViewById<TextView>(R.id.email)
        emailView.text = usr.email

        val locationView = findViewById<TextView>(R.id.location)
        locationView.text = usr.location

        val balanceView = findViewById<TextView>(R.id.balance)
        balanceView.text = usr.balance.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_showprofileactivity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option1 -> {
                Toast.makeText(
                    applicationContext, "Edit profile",
                    Toast.LENGTH_SHORT
                ).show()
                editProfile() //evoked when the pencil button is pressed

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun editProfile() {
        val i = Intent(this, EditProfileActivity::class.java)
        i.putExtra("user", usr)
        startActivityForResult(i, LAUNCH_EDIT_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("result code: $resultCode")
        if(resultCode >  -1000) {
            when(requestCode){
                LAUNCH_EDIT_ACTIVITY  -> usr.fullName = data?.getStringExtra("user").toString()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("user", usr)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        usr = savedInstanceState.getSerializable("user") as User
        displayUser(usr)
    }

}