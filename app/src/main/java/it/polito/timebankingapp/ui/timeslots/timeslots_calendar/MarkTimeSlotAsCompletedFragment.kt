package it.polito.timebankingapp.ui.timeslots.timeslots_calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.chats.chat.ChatViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel

class MarkTimeSlotAsCompletedFragment : Fragment(R.layout.fragment_mark_time_slot_as_completed) {

    private val vm: TimeSlotsViewModel by activityViewModels()
    private val chatVm: ChatViewModel by activityViewModels()


    lateinit var v: View;
    lateinit var undoButton: Button
    lateinit var confirmButton: Button
    lateinit var tsTitleText: TextView
    lateinit var timeslot: TimeSlot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timeslot = arguments?.getSerializable("timeslot") as TimeSlot
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        undoButton = v.findViewById(R.id.completed_undo_button)
        confirmButton= v.findViewById(R.id.completed_confirm_button)
        tsTitleText= v.findViewById(R.id.completed_ts_title)

        tsTitleText.text = timeslot.title

        undoButton.setOnClickListener {
            Toast.makeText(requireContext(),
                "Aborting...", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        confirmButton.setOnClickListener {
            //IN QUESTA RIGA DI CODICE CI VA IL METODO DELLA VM CON LA QUERY!!!
            vm.setTimeSlotAsCompleted(timeslot)


            Toast.makeText(requireContext(),
                "Marked as completed!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}
