package it.polito.timebankingapp.ui.skillslist

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import it.polito.timebankingapp.R

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        val chipGroup: ChipGroup = v.findViewById(R.id.skillsGroup)

        SKILLS.forEach { skill ->
            val chip = layoutInflater.inflate(
                R.layout.chip_layout_show,
                chipGroup.parent.parent as ViewGroup,
                false
            ) as Chip
            chip.text = skill
            chipGroup.addView(chip)
        }
        for (i in 0..chipGroup.childCount-1) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.setOnClickListener { view ->
                val text = (view as Chip).text.toString()
                val b = bundleOf("skill" to text)
                findNavController().navigate(R.id.action_nav_skillsList_to_skillSpecificTimeSlotListFragment, b)
            }
        }
    }
}