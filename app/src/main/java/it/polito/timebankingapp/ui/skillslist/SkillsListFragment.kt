package it.polito.timebankingapp.ui.skillslist

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.R
import it.polito.timebankingapp.ui.auth.LoginFragment
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import it.polito.timebankingapp.ui.timeslots.timeslots_list.TimeSlotAdapter

/* Global lists of skills,
   Every-time a user add a new skill in his profile, if not present in this list, it will be added!  */
private var SKILLS = arrayOf(
    "Gardening",
    "Tutoring",
    "Baby sitting",
    "Driver",
    "C developer",
    "Grocery shopping",
    "Cleaning and organization",
    "Cooking",
    "Data analytics",
    "Microsoft Excel",
)

class SkillsListFragment : Fragment(R.layout.fragment_skills_list) {

    private lateinit var v : View
    private val authVm: ProfileViewModel by activityViewModels()
    private val vm: TimeSlotsViewModel by activityViewModels()


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController()

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) { success ->
                if (!success) {
                    val startDestination = navController.graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            }
    }*/




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        val chipGroup: ChipGroup = v.findViewById(R.id.skillsGroup)

        val voidMessageImage = view.findViewById<ImageView>(R.id.time_slot_icon)
        val voidMessageText = view.findViewById<TextView>(R.id.emptyListMessage)
        val voidMessageSubText = view.findViewById<TextView>(R.id.empty_list_second_message)
        val progressBar = view.findViewById<ProgressBar>(R.id.skill_list_progressBar)

        authVm.fireBaseUser.observe(viewLifecycleOwner){
            if(it != null) {
                vm.updatePerSkillTimeSlots()
                vm.updatePersonalTimeSlots()
            }
        }

        vm.skillList.observe(viewLifecycleOwner){
            progressBar.visibility = View.GONE
            if(it.isNotEmpty()){
                it.forEach { skill ->
                    val chip = layoutInflater.inflate(
                        R.layout.chip_layout_show,
                        chipGroup.parent.parent as ViewGroup,
                        false
                    ) as Chip
                    chip.text = skill
                    chip.setOnClickListener { ch ->
                        val text = (ch as Chip).text.toString()
                        vm.setFilteringSkill( text)
                        //val b = bundleOf("skill" to text)
                        findNavController().navigate(R.id.action_nav_skillsList_to_skillSpecificTimeSlotListFragment)
                    }
                    chipGroup.addView(chip)
                }
            }
            else{
                voidMessageText.isVisible = true
                voidMessageImage.isVisible = true
                voidMessageSubText.isVisible = true
            }
        }



        /*authVm.fireBaseUser.observe(viewLifecycleOwner){
            if(it == null) {
                findNavController().navigate(R.id.action_nav_skillsList_to_nav_login)
            }
        }*/

/*
        SKILLS.forEach { skill ->
            val chip = layoutInflater.inflate(
                R.layout.chip_layout_show,
                chipGroup.parent.parent as ViewGroup,
                false
            ) as Chip
            chip.text = skill
            chip.setOnClickListener { ch ->
                val text = (ch as Chip).text.toString()
                val b = bundleOf("skill" to text)
                findNavController().navigate(R.id.action_nav_skillsList_to_skillSpecificTimeSlotListFragment,b)
            }
            chipGroup.addView(chip)
        }
*/
    }
}