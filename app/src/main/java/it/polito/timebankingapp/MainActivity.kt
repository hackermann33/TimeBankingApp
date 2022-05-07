package it.polito.timebankingapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.databinding.ActivityMainBinding
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import java.io.File
import java.io.FileInputStream


interface DrawerController {
    fun setDrawerLocked();
    fun setDrawerUnlocked();
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
        var navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_personalTimeSlotsList, /*, R.id.nav_timeSlotDetails*/
                R.id.nav_showProfile,
                R.id.nav_login,
                R.id.nav_skillsList //nota bug: la freccia direzionale non torna indietro ma apre il drawer
            ),
            drawerLayout
        )

        //NON RIMUOVERE ANCHE SE INUTILIZZATA
        var toggle = ActionBarDrawerToggle(this, drawerLayout, binding.appBarMain.toolbar, R.string.drawer_open, R.string.drawer_close);

        navController.addOnDestinationChangedListener{_, destination, _ ->
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
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        vm.usr.observe(this) {

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

        }



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

}