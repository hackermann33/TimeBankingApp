package it.polito.timebankingapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.Serializable
import com.google.gson.GsonBuilder


class ShowProfileActivity : AppCompatActivity() {

    private lateinit var usr: User
    private val LAUNCH_EDIT_ACTIVITY = 1

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getPreferences(android.content.Context.MODE_PRIVATE)

        /* All these info will be retrieved from server */
        val proPic = BitmapFactory.decodeResource(baseContext.resources, R.drawable.default_avatar)
        val fullName = "Name Surname"
        val nick = "example"
        val email = "example@test.com"
        val location = "45.070312, 7.6868565"
        val skills: List<String> = mutableListOf()
        val balance = 3
        val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."

        val profile = sharedPref.getString("profile", "")
        if (sharedPref.contains("profile")) {
            usr = GsonBuilder().create().fromJson(profile, User::class.java)
            usr.init = true
        } else {
            usr = User(" ", fullName, nick, email, location, skills, balance, description)
        }

        setContentView(R.layout.activity_showprofileactivity)


        /*picView = findViewById(R.id.profile_pic)
        val bMap: Bitmap = BitmapFactory.decodeFile(picPath)
        picView.setImageBitmap(bMap);*/

        displayUser()

    }

    private fun displayUser(){

        val profilePic = findViewById<CircleImageView>(R.id.profile_pic)
        try {
            val f = File(usr.pic, "profile.jpg")
            val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
            profilePic.setImageBitmap(bitmap)
        }
        catch (e : FileNotFoundException){
            e.printStackTrace()
        }
        //profilePic.setImageBitmap()

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

        val descriptionView = findViewById<TextView>(R.id.description)
        descriptionView.text = usr.description
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
                editProfile() //evoked when the pencil button is pressed
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun editProfile() {
        val i = Intent(this, EditProfileActivity::class.java)
        i.putExtra("it.polito.timebankingapp.ShowProfileActivity.user", usr)
        startActivityForResult(i, LAUNCH_EDIT_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("result code: $resultCode")
        if (requestCode == LAUNCH_EDIT_ACTIVITY && resultCode == Activity.RESULT_OK){
            usr = data?.getSerializableExtra("it.polito.timebankingapp.EditProfileActivity.user") as User
            displayUser()
            val jsonString = GsonBuilder().create().toJson(usr)
            with (sharedPref.edit()) {
                putString("profile", jsonString)
                apply()
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
        displayUser()
    }

}