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

class SkillsListFragment : Fragment(R.layout.fragment_skills_list) {

    private lateinit var v : View
    private val authVm: ProfileViewModel by activityViewModels()
    private val vm: TimeSlotsViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        val chipGroup: ChipGroup = v.findViewById(R.id.skillsGroup)

        val voidMessageImage = view.findViewById<ImageView>(R.id.time_slot_icon)
        val voidMessageText = view.findViewById<TextView>(R.id.emptyListMessage)
        val voidMessageSubText = view.findViewById<TextView>(R.id.empty_list_second_message)
        val progressBar = view.findViewById<ProgressBar>(R.id.skill_list_progressBar)


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

        }




    }
}