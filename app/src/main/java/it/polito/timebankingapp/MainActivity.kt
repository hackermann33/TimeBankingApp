package it.polito.timebankingapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
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
import it.polito.timebankingapp.ui.chats.ChatViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


class MainActivity : AppCompatActivity() {

    /*
    val vm by viewModels<TimeSlotsListViewModel>()
    private val sharedModel by viewModels<TimeSlotSharedViewModel>()
     */

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private val vm by viewModels<ProfileViewModel>()
    private val timeSlotVm by viewModels<TimeSlotsViewModel>()
    private val chatVm by viewModels<ChatViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView



        navController = findNavController(R.id.nav_host_fragment_content_main)

        /* Considering the SkillsListFragment (as described in the navigation graph)
        as top-level simplify navigation */
        appBarConfiguration = AppBarConfiguration(
            /*setOf(R.id.nav_skillsList, R.id.nav_showProfile, R.id.nav_personalTimeSlotsList),*/
            navController.graph,
            drawerLayout
        )

        //NON RIMUOVERE ANCHE SE INUTILIZZATA
        /*var toggle = ActionBarDrawerToggle(this, drawerLayout, binding.appBarMain.toolbar, R.string.drawer_open, R.string.drawer_close)*/

        /* Disabling drawer when in login fragment */
        navController.addOnDestinationChangedListener { nc, destination, _ ->
            if (destination.id == R.id.nav_login || !isInsideDrawer(destination.id)) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                //toggle.isDrawerIndicatorEnabled = false;
                //binding.appBarMain.toolbar.navigationIcon = null
                if (destination.id == R.id.nav_login)
                    binding.appBarMain.toolbar.visibility = View.GONE
            } else if (nc.backQueue.size > 3)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            else {
                binding.appBarMain.toolbar.visibility = View.VISIBLE
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                //toggle.isDrawerIndicatorEnabled = true;
                /* Se vado a nav_showProfile ma ho giànav_timeSlotDetails nello stack... sono TimeSlotProfile*/
            }

            /* Here I'm downloading the right infos when I navigate from drawer*/
            if(destination.id == R.id.nav_personalTimeSlotList){
                timeSlotVm.updatePersonalTimeSlots()
            }
            if(destination.id == R.id.nav_interestingTimeSlotList){
                timeSlotVm.updateInterestingTimeSlots()
            }
            if(destination.id == R.id.nav_allChatsList)
                chatVm.updateAllChats()
        }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val signOutButton = navView.getHeaderView(0).findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            setSignOutClick()
        }

        /* If user signs out, navigation to login fragment will happen */
        vm.fireBaseUser.observe(this) {
            if (it == null) {
                navController.navigate(R.id.action_global_to_nav_login)
            }
        }

        val progressBar =
            navView.getHeaderView(0).findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
        vm.user.observe(this) {
            val fullName = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)
            val emailET = navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView)
            val balance = navView.getHeaderView(0).findViewById<TextView>(R.id.balance)
            if (it != null) {
                fullName.text = it.fullName
                emailET.text = it.email
                balance.text = it.balance.toString().plus(" hours")

                if(!it.hasImage())
                    progressBar.visibility = View.GONE

            }
        }


        val profilePic =
            navView.getHeaderView(0).findViewById<CircleImageView>(R.id.profile_pic)

        vm.userImage.observe(this) {
            if (it != null) {
                profilePic.setImageBitmap(it)
                progressBar.visibility = View.GONE
            } else {
                profilePic.setImageResource(R.drawable.default_avatar)
            }
        }


    }

    private fun isInsideDrawer(@IdRes id: Int): Boolean {
        return id == R.id.nav_skillsList || id == R.id.nav_showProfile || id == R.id.nav_skillSpecificTimeSlotList
    }


    private fun setSignOutClick() {
        vm.logOut()
        Firebase.auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.my_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = this.let { GoogleSignIn.getClient(it, gso) }
        googleSignInClient.signOut();
        //navController.navigate(R.id.action_global_to_nav_login)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setActionBarTitle(title: String?) {
        supportActionBar?.title = title
    }
}

