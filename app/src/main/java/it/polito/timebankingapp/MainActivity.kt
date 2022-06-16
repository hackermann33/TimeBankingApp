package it.polito.timebankingapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
//import com.jakewharton.threetenabp.AndroidThreeTen
import it.polito.timebankingapp.databinding.ActivityMainBinding
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.ui.chats.chatslist.ChatListViewModel

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

    private lateinit var pBUserPic: ProgressBar
    private lateinit var profilePic: ImageView

    val vm by viewModels<ProfileViewModel>()
    val timeSlotVm by viewModels<TimeSlotsViewModel>()
    val chatListVm by viewModels<ChatListViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        //AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val counterView = navView.menu.findItem(R.id.nav_personalTimeSlotList).actionView.findViewById<TextView>(R.id.counter)
        val chatsView = navView.menu.findItem(R.id.nav_allChatsList).actionView.findViewById<TextView>(R.id.counter)

//        timeSlotVm.updatePersonalTimeSlots()
        chatListVm.updateAllChats()

        /*chatListVm.unreadChats.observe(this){
            if(it > 0){
                chatsView.text = it.toString()
                chatsView.visibility = View.VISIBLE
                counterView.text = ""
                counterView.visibility = View.VISIBLE
            }
            else {
                counterView.visibility = View.GONE
                chatsView.visibility = View.GONE
            }
        }*/



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
                /* Se vado a nav_showProfile ma ho già nav_timeSlotDetails nello stack... sono TimeSlotProfile*/
            }

            /* Here I'm downloading the right infos when I navigate from drawer*/
            if(destination.id == R.id.nav_personalTimeSlotList){
                timeSlotVm.updatePersonalTimeSlots()
            }
            if(destination.id == R.id.nav_interestingTimeSlotList){
                timeSlotVm.updateInterestingTimeSlots()
            }
            if(destination.id == R.id.nav_completedTimeSlotList){
                timeSlotVm.updateCompletedTimeSlots()
                //timeSlotVm.updateInterestingTimeSlots()
            }
            if(destination.id == R.id.nav_timeSlotMonthCalendar){
                timeSlotVm.updateAssignedTimeSlots()
            }
            if(destination.id == R.id.nav_allChatsList)
                chatListVm.updateAllChats()
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

        pBUserPic =
            navView.getHeaderView(0).findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
        profilePic =
            navView.getHeaderView(0).findViewById<ImageView>(R.id.fragment_show_profile_iv_profile_pic)


        vm.user.observe(this) {
            val fullName = navView.getHeaderView(0).findViewById<TextView>(R.id.fullName)
            var emailET = navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView)
            val tvBalance = navView.getHeaderView(0).findViewById<TextView>(R.id.balance)
            if (it != null) {
                fullName.text = it.fullName
                emailET.text = it.email
                tvBalance.text = it.balance.toString().plus(" hour(s)")
                Helper.loadImageIntoView(profilePic, pBUserPic, it.profilePicUrl)
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
        profilePic.setImageResource(R.drawable.default_avatar)
        pBUserPic.visibility = View.VISIBLE 
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

