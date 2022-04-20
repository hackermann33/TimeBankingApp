package it.polito.timebankingapp

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import it.polito.timebankingapp.databinding.ActivityMainBinding
import it.polito.timebankingapp.ui.timeslot_details.TimeSlotSharedViewModel
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslot_edit.TimeSlotEditFragment
import it.polito.timebankingapp.ui.timeslots_list.TimeSlotsListViewModel

class MainActivity : AppCompatActivity() {
    val vm by viewModels<TimeSlotsListViewModel>()
    private val sharedModel by viewModels<TimeSlotSharedViewModel>()


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.addTimeSlotButton.setOnClickListener { view ->
/*            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)

 */
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_timeSlotListFragment_to_nav_timeSlotEdit)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_timeSlotsList /*, R.id.nav_timeSlotDetails*/
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedModel.select(TimeSlot().also {
            it.title = "TitleTrial"; it.description = "Descr trial"; it.date = "2022/12/18"; it.time = "14:15"; it.duration = "56"; it.location = "Turin"
        })

        //createItems(10)

    }


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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun finish() {
        vm.clear()
        super.finish()
    }



}