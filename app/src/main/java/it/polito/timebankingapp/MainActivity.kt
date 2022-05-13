package it.polito.timebankingapp

import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.databinding.ActivityMainBinding
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import java.io.File
import java.io.FileInputStream


interface DrawerController {
    fun setDrawerLocked()
    fun setDrawerUnlocked()
}


class MainActivity : AppCompatActivity()/*, DrawerController */{
    /*
    override fun setDrawerLocked(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun setDrawerUnlocked(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }*/

    /*
    val vm by viewModels<TimeSlotsListViewModel>()
    private val sharedModel by viewModels<TimeSlotSharedViewModel>()
     */

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    val vm by viewModels<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_skillsList, //nota bug: la freccia direzionale non torna indietro ma apre il drawer
                R.id.nav_personalTimeSlotsList, /*, R.id.nav_timeSlotDetails*/
                R.id.nav_showProfile
            ),
            drawerLayout
        )

        //NON RIMUOVERE ANCHE SE INUTILIZZATA
        /*var toggle = ActionBarDrawerToggle(this, drawerLayout, binding.appBarMain.toolbar, R.string.drawer_open, R.string.drawer_close)*/

        /*navController.addOnDestinationChangedListener{_, destination, _ ->
            if (destination.id == R.id.nav_login) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                //toggle.isDrawerIndicatorEnabled = false;
                //binding.appBarMain.toolbar.navigationIcon = null
                binding.appBarMain.toolbar.visibility = View.GONE
            } else {
                binding.appBarMain.toolbar.visibility = View.VISIBLE
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                //toggle.isDrawerIndicatorEnabled = true;
            }
        }*/


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val signOutButton = navView.getHeaderView(0).findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener{
            vm.logOut()
            Firebase.auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.my_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = this.let { GoogleSignIn.getClient(it, gso) }

            googleSignInClient.signOut();
        }

        vm.user.observe(this) {
            val fullName = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)
            if(it!= null) {
                fullName.text = it.fullName
            }
        }

        val progressBar = navView.getHeaderView(0).findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
        val profilePic =
            navView.getHeaderView(0).findViewById<CircleImageView>(R.id.profile_pic)

        vm.userImage.observe(this){
            if(it != null) {
                profilePic.setImageBitmap(it)
                progressBar.visibility = View.GONE
            }
            else {
                profilePic.setImageResource(R.drawable.default_avatar)
            }
        }

        if(vm.userImage.value == null)
            progressBar.visibility = View.GONE


        /*vm.message.observe(this, Observer {
            var usr = vm.user.value!!
            val fullName = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)
            val profilePic = navView.getHeaderView(0).findViewById<CircleImageView>(R.id.profile_pic)
            val progressBar = navView.getHeaderView(0).findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
            fullName.text = usr.fullName
            if(usr.tempImagePath == "") {
                val cw = ContextWrapper(this)
                vm.retrieveAndSetProfilePic(usr, profilePic, progressBar, cw)
            }else {
                progressBar.visibility = View.GONE
                val f = File(usr.tempImagePath) //loggedUser.photoUrl (già salvata in locale)
                val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
                profilePic.setImageBitmap(bitmap)
            }
            *//*it.getContentIfNotHandled()?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }*//*
        })*/

        if(vm.fireBaseUser.value != null) {
            val fullName = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)
        }

        /*vm.usr.observe(this) {

            val profilePic = navView.getHeaderView(0).findViewById<CircleImageView>(R.id.profile_pic)
            val fullName = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)

            if (it != null) {
                fullName.text = it.fullName
                try {
                    val f = File(it.pic)
                    val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
                    profilePic.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }*/



/*
        sharedModel.select(TimeSlot().also {
            it.title = "TitleTrial"; it.description = "Descr trial"; it.date = "2022/12/18"; it.time = "14:15"; it.duration = "56"; it.location = "Turin"
        })

 */

        //createItems(10)

    }


    /*
        private fun createItems(n: Int): MutableList<TimeSlot> {
            val l = mutableListOf<TimeSlot>()
            for (i in 1..n) {
                TimeSlot()
                val ts = TimeSlot().also{
                    it.title = "TitleTrial $i";
                    it.description= "Descr trial $i";
                    it.date = "2022/12/18";
                    it.time = "14:15";
                    it.duration = "56";
                    it.location = "Turin";
                }
                l.add(ts)
                vm.addTimeSlot(ts)
            }
            return l
        }
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setActionBarTitle(title: String?) {
         supportActionBar?.title = title
    }
}