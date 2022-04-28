package it.polito.timebankingapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
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
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    /*
    val vm by viewModels<TimeSlotsListViewModel>()
    private val sharedModel by viewModels<TimeSlotSharedViewModel>()
     */

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
                R.id.nav_timeSlotsList, /*, R.id.nav_timeSlotDetails*/
                R.id.nav_showProfile
            ),
            drawerLayout
        )
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