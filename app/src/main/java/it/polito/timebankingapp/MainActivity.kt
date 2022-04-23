package it.polito.timebankingapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import it.polito.timebankingapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    /*
    val vm by viewModels<TimeSlotsListViewModel>()
    private val sharedModel by viewModels<TimeSlotSharedViewModel>()
     */

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
                R.id.nav_timeSlotsList, /*, R.id.nav_timeSlotDetails*/
                R.id.nav_showProfile
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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