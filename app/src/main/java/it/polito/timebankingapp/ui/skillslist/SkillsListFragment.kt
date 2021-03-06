package it.polito.timebankingapp.ui.skillslist

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import it.polito.timebankingapp.R
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel

class SkillsListFragment : Fragment(R.layout.fragment_skills_list) {

    private lateinit var v : View
    private val vm: TimeSlotsViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        //val chipGroup: ChipGroup = v.findViewById(R.id.skillsGroup)
        val chipFlexGroup: FlexboxLayout = v.findViewById(R.id.skillsFlexGroup)
        val progressBar = view.findViewById<ProgressBar>(R.id.skill_list_progressBar)



        val nightModeFlags = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> chipFlexGroup.setDividerDrawable( ResourcesCompat.getDrawable( resources, R.drawable.flexbox_divider_dark_mode, null))
            Configuration.UI_MODE_NIGHT_NO -> chipFlexGroup.setDividerDrawable( ResourcesCompat.getDrawable( resources, R.drawable.flexbox_divider, null))
            Configuration.UI_MODE_NIGHT_UNDEFINED -> chipFlexGroup.setDividerDrawable( ResourcesCompat.getDrawable( resources, R.drawable.flexbox_divider, null))
        }

        vm.skillList.observe(viewLifecycleOwner){
            progressBar.visibility = View.GONE
            if(it.isNotEmpty()){
                it.forEach { skill ->
                    /*val chip = layoutInflater.inflate(
                        R.layout.chip_layout_show,
                        chipGroup.parent.parent as ViewGroup,
                        false
                    ) as Chip
                    chip.text = skill
                    chip.setOnClickListener { ch ->
                        val text = (ch as Chip).text.toString()
                        //vm.setType("skill")
                        //vm.setType("skill", skill)
                        vm.setFilteringSkill(skill)
                        val b = bundleOf("point_of_origin" to "skill_specific")

                        /* This check is useful to avoid crash on emulator*/
                        if(findNavController().currentDestination?.id==R.id.nav_skillsList)
                            findNavController().navigate(R.id.action_nav_skillsList_to_skillSpecificTimeSlotListFragment, b)
                    }
                    chipGroup.addView(chip)*/
                    //
                    val chip = layoutInflater.inflate(
                        R.layout.chip_layout_show,
                        chipFlexGroup.parent.parent as ViewGroup,
                        false
                    ) as Chip
                    chip.text = skill
                    chip.setOnClickListener { ch ->
                        val text = (ch as Chip).text.toString()
                        //vm.setType("skill")
                        //vm.setType("skill", skill)
                        vm.setFilteringSkill(skill)
                        val b = bundleOf("point_of_origin" to "skill_specific")

                        /* This check is useful to avoid crash on emulator*/
                        if(findNavController().currentDestination?.id==R.id.nav_skillsList)
                            findNavController().navigate(R.id.action_nav_skillsList_to_skillSpecificTimeSlotListFragment, b)
                    }
                    chipFlexGroup.addView(chip)
                }
            }

        }




    }
}