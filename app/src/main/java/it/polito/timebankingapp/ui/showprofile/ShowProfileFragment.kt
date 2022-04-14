package it.polito.timebankingapp.ui.showprofile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import com.google.gson.GsonBuilder
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.editprofile.EditProfileActivity


const val LAUNCH_EDIT_ACTIVITY = 1


class ShowProfileActivity : AppCompatActivity() {
    private lateinit var usr: User

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getPreferences(android.content.Context.MODE_PRIVATE)


        val profile = sharedPref.getString("profile", "")
        usr = if (sharedPref.contains("profile")) GsonBuilder().create()
            .fromJson(profile, User::class.java)
        else User()

        setContentView(R.layout.activity_showprofileactivity)

        displayUser()

    }

    private fun displayUser() {

        val profilePic = findViewById<CircleImageView>(R.id.profile_pic)
        val sv = findViewById<ScrollView>(R.id.scrollView2)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            sv.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val h = sv.height
                    val w = sv.width
                    profilePic.post {
                        profilePic.layoutParams = LinearLayout.LayoutParams(w, h / 3)
                    }
                    sv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        try {
            val f = File(usr.pic)
            val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
            profilePic.setImageBitmap(bitmap)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val nameView = findViewById<TextView>(R.id.fullName)
        nameView.text = usr.fullName

        val nickView = findViewById<TextView>(R.id.nickname)
        nickView.text = usr.nick

        val emailView = findViewById<TextView>(R.id.email)
        emailView.text = usr.email

        val locationView = findViewById<TextView>(R.id.time_slot_time)
        locationView.text = usr.location

        val balanceView = findViewById<TextView>(R.id.balance)
        balanceView.text = usr.balance.toString()

        val descriptionView = findViewById<TextView>(R.id.description)
        descriptionView.text = usr.description

        val chipGroup = findViewById<ChipGroup>(R.id.skillsGroup)

        chipGroup.removeAllViews()
        usr.skills.forEach { skill ->
            val chip = layoutInflater.inflate(
                R.layout.chip_layout_showprofile,
                chipGroup!!.parent.parent as ViewGroup,
                false
            ) as Chip
            chip.text = skill
            chipGroup.addView(chip)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_editpencil, menu)
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
        if (requestCode == LAUNCH_EDIT_ACTIVITY && resultCode == Activity.RESULT_OK) {
            usr =
                data?.getSerializableExtra("it.polito.timebankingapp.EditProfileActivity.user") as User
            displayUser()
            val jsonString = GsonBuilder().create().toJson(usr)
            with(sharedPref.edit()) {
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
