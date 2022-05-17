package it.polito.timebankingapp.ui.timeslots.timeslots_list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


/**
 * A fragment representing a list of Items.
 */
class TimeSlotListFragment : Fragment(R.layout.fragment_timeslots_list) {

    private lateinit var type: String
    private var columnCount = 1 //credo sia da rimuovere
    private var filterParameter = "Title"
    private var filterKeywords = ""
    private var orderingDirection = false //false == ascending, true = descending
    private var openFilterSortMenu = false

    val vm : TimeSlotsViewModel by activityViewModels()
    private lateinit var rv:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("point_of_origin").toString() //skill or personal
        if(type == "skill")
            setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.time_slot_list)
        rv.layoutManager = LinearLayoutManager(context)

        val addTimeSlotButton = view.findViewById<FloatingActionButton>(R.id.addTimeSlotButton)


        val voidMessageImage = view.findViewById<ImageView>(R.id.time_slot_icon)
        val voidMessageText = view.findViewById<TextView>(R.id.emptyListMessage)
        val voidMessageSubText = view.findViewById<TextView>(R.id.empty_list_second_message)

        if(type == "skill") {
            addTimeSlotButton.visibility = View.GONE


            var adTmp = TimeSlotAdapter(
                vm.perSkillTimeSlots.value?.toMutableList() ?: mutableListOf(),
                ::selectTimeSlot,
                "skill_specific"
            )
            rv.adapter = adTmp

            //var skill = arguments?.getString("skill")

            vm.perSkillTimeSlots.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    voidMessageText.isVisible = false
                    voidMessageImage.isVisible = false
                    voidMessageSubText.isVisible = false

                    adTmp = TimeSlotAdapter(it.toMutableList(), ::selectTimeSlot, "skill_specific")
                    adTmp.data = it.toMutableList()
                    rv.adapter = adTmp
                    adTmp.setFilter(filterKeywords, filterParameter)
                    adTmp.setOrder(filterParameter, orderingDirection)
                } else {
                    voidMessageText.isVisible = true
                    voidMessageImage.isVisible = true
                    voidMessageSubText.isVisible = true

                }
            }


            /*
        val adapter= TimeSlotAdapter(l)
        rv.adapter = adapter
        */

            val spinner: Spinner = view.findViewById(R.id.filter_spinner)
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter.createFromResource(
                requireActivity().applicationContext,
                R.array.filter_parameters_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }


            //combobox
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    filterParameter =
                        resources.getStringArray(R.array.filter_parameters_array)[position];
                    adTmp.setFilter(filterKeywords, filterParameter)
                    adTmp.setOrder(filterParameter, orderingDirection)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val searchView: SearchView = view.findViewById(R.id.filter_bar)
            //barra di ricerca
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(s: String): Boolean {
                    filterKeywords = s
                    adTmp.setFilter(filterKeywords, filterParameter)
                    adTmp.setOrder(filterParameter, orderingDirection)
                    return false
                }
            })

            //image buttons (sorting)
            val ascendingButton = view.findViewById(R.id.ascend_button) as ImageButton
            val descendingButton = view.findViewById(R.id.descend_button) as ImageButton
            val mRecyclerView = view.findViewById(R.id.time_slot_list) as RecyclerView
            val layoutManager = mRecyclerView
                .layoutManager as LinearLayoutManager?

            ascendingButton.setOnClickListener {
                orderingDirection = false
                adTmp.setFilter(filterKeywords, filterParameter)
                adTmp.setOrder(filterParameter, orderingDirection)
                layoutManager!!.scrollToPositionWithOffset(0, 0)
            }

            descendingButton.setOnClickListener {
                orderingDirection = true
                adTmp.setFilter(filterKeywords, filterParameter)
                adTmp.setOrder(filterParameter, orderingDirection)
                layoutManager!!.scrollToPositionWithOffset(0, 0)
            }

        }
        else { //personal

            var adTmp = TimeSlotAdapter(vm.personalTimeSlots.value?.toMutableList() ?: mutableListOf(), ::selectTimeSlot, "personal")
            rv.adapter = adTmp

            vm.personalTimeSlots.observe(viewLifecycleOwner){
                if(it.isNotEmpty()){
                    voidMessageText.isVisible = false
                    voidMessageImage.isVisible = false
                    voidMessageSubText.isVisible = false

                    adTmp = TimeSlotAdapter(it.toMutableList(), ::selectTimeSlot, "personal")
                    adTmp.data = it.toMutableList()
                    rv.adapter = adTmp
                }
                else{
                    voidMessageText.isVisible = true
                    voidMessageImage.isVisible = true
                    voidMessageSubText.isVisible = true

                }
            }
            addTimeSlotButton.setOnClickListener { _ ->
/*            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
 */
                findNavController().navigate(R.id.action_nav_skillSpecificTimeSlotList_to_nav_timeSlotEdit)
            }

        }

        setFragmentResultListener("timeSlot") { _, bundle ->
            val result = bundle.getInt("timeSlotConfirm")

            if (result == 1) {
                val snackBar = Snackbar.make(
                    view,
                    "New time slot successfully added.",
                    Snackbar.LENGTH_LONG
                )
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            } else if (result == 2) {
                val snackBar =
                    Snackbar.make(view, "Time slot successfully edited.", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
        }


    }



    private fun selectTimeSlot(ts: TimeSlot) {
        vm.setSelectedTimeSlot(ts)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_filter_and_sort, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_and_sort_option -> {
                /* filter by something*/
                val linearLayout = view?.findViewById(R.id.filter_and_sort_layout) as LinearLayout
                if(!openFilterSortMenu) {
                    linearLayout.visibility = View.VISIBLE
                    openFilterSortMenu = true
                }
                else {
                    linearLayout.visibility = View.GONE
                    openFilterSortMenu = false
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}