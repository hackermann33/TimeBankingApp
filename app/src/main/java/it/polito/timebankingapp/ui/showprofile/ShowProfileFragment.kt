package it.polito.timebankingapp.ui.showprofile

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import com.google.gson.GsonBuilder
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.user.User

class ShowProfileFragment : Fragment(R.layout.fragment_showprofile) {

    private lateinit var usr: User
    private lateinit var sharedPref: SharedPreferences
    private lateinit var v : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        sharedPref = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE)

        val profile = sharedPref.getString("profile", "")
        usr = if (sharedPref.contains("profile")) GsonBuilder().create()
            .fromJson(profile, User::class.java)
        else User()

        //usr = savedInstanceState?.getSerializable("user") as User

        setFragmentResultListener("profile") { requestKey, bundle ->
            usr = bundle.getSerializable("user") as User
            showProfile(view)
            val jsonString = GsonBuilder().create().toJson(usr)
            with(sharedPref.edit()) {
                putString("profile", jsonString)
                apply()
            }
        }

        showProfile(view)
    }

    private fun showProfile(view: View) {
        val profilePic = view.findViewById<CircleImageView>(R.id.profile_pic)
        val sv = view.findViewById<ScrollView>(R.id.scrollView2)

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

        val nameView = view.findViewById<TextView>(R.id.fullName)
        nameView.text = usr.fullName

        val nickView = view.findViewById<TextView>(R.id.nickname)
        nickView.text = usr.nick

        val emailView = view.findViewById<TextView>(R.id.email)
        emailView.text = usr.email

        val locationView = view.findViewById<TextView>(R.id.location)
        locationView.text = usr.location

        val balanceView = view.findViewById<TextView>(R.id.balance)
        balanceView.text = usr.balance.toString()

        val descriptionView = view.findViewById<TextView>(R.id.description)
        descriptionView.text = usr.description

        val chipGroup = view.findViewById<ChipGroup>(R.id.skillsGroup)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editpencil, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                Toast.makeText(
                    context, "Edit TimeSlot",
                    Toast.LENGTH_SHORT
                ).show()
                editProfile() //evoked when the pencil button is pressed
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        //launch edit profile fragment
        val b = bundleOf("profile" to usr)
        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileActivity, b)
    }

}
