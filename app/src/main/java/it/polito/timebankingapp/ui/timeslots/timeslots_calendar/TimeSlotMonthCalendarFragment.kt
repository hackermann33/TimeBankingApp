package it.polito.timebankingapp.ui.timeslots.timeslots_calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import it.polito.timebankingapp.R
import it.polito.timebankingapp.databinding.FragmentTimeSlotMonthCalendarBinding
import it.polito.timebankingapp.databinding.TimeSlotMonthCalendarDayBinding
import it.polito.timebankingapp.databinding.TimeSlotMonthCalendarHeaderBinding
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class TimeSlotMonthCalendar : Fragment(R.layout.fragment_time_slot_month_calendar) {

    private val vm: TimeSlotsViewModel by activityViewModels()
    private val userVm: ProfileViewModel by activityViewModels()


    private val eventsAdapter = TimeSlotMonthCalendarEventsAdapter()
/*
    private val inputDialog by lazy {
        val editText = AppCompatEditText(requireContext())
        val layout = FrameLayout(requireContext()).apply {
            // Setting the padding on the EditText only pads the input area
            // not the entire EditText so we wrap it in a FrameLayout.
            val padding = dpToPx(20, requireContext())
            setPadding(padding, padding, padding, padding)
            addView(editText, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.example_3_input_dialog_title))
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                saveEvent(editText.text.toString())
                // Prepare EditText for reuse.
                editText.setText("")
            }
            .setNegativeButton(R.string.close, null)
            .create()
            .apply {
                setOnShowListener {
                    // Show the keyboard
                    editText.requestFocus()
                    context.inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                }
                setOnDismissListener {
                    // Hide the keyboard
                    context.inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
            }
    }*/


    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val events = mutableMapOf<LocalDate, List<Event>>()

    private lateinit var binding: FragmentTimeSlotMonthCalendarBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding = FragmentTimeSlotMonthCalendarBinding.bind(view)
        binding.exThreeRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }


        val nightModeFlags = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.exThreeSelectedDateText.setTextColor(Color.parseColor("#ffffff"));
                binding.exThreeSelectedDateText.setBackgroundColor(Color.parseColor("#333333"));
                //binding.exThreeCalendar.setBackgroundColor(Color.parseColor("#2c2c2c"))
            }
        }

        vm.timeSlots.observe(viewLifecycleOwner) { it ->
            events.clear()
            if (!it.isNullOrEmpty()) {
                Log.d("CalendarFragment", "Rendering...$it")
                //setVoidMessage(view, false)
                val acceptedTimeSlots = it!!

                for (i in it.indices) {
                    val values = acceptedTimeSlots[i].date.split("/")
                    val tempDate = LocalDate.of(values[2].toInt(), values[1].toInt(),values[0].toInt())
                    tempDate?.let {
                        val isOffered = userVm.user.value?.id ?: "" == acceptedTimeSlots[i].userId
                        events[it] = events[it].orEmpty().plus(Event(UUID.randomUUID().toString(), acceptedTimeSlots[i], it, isOffered))
                        refreshDate(it)
                        if(i == acceptedTimeSlots.size-1) {
                            //refreshDate(LocalDate.now())
                            updateAdapterForDate(LocalDate.now())
                        }
                    }
                }
            } else {
                updateAdapterForDate(LocalDate.now())
            }

        }

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        binding.exThreeCalendar.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null) {
            binding.exThreeCalendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = TimeSlotMonthCalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }
        binding.exThreeCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.exThreeDayText
                val dotView = container.binding.exThreeDotView

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        today -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            dotView.makeInVisible()
                        }
                        selectedDate -> {
                            textView.setTextColorRes(R.color.dark_blue)
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            dotView.makeInVisible()
                        }
                        else -> {
                            when (nightModeFlags) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    textView.setTextColorRes(R.color.white)
                                }
                                Configuration.UI_MODE_NIGHT_NO -> { textView.setTextColorRes(R.color.opaque_black) }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> { textView.setTextColorRes(R.color.opaque_black) }
                            }
                            textView.background = null
                            dotView.isVisible = events[day.date].orEmpty().isNotEmpty()
                        }
                    }
                } else {
                    textView.makeInVisible()
                    dotView.makeInVisible()
                }
            }
        }

        binding.exThreeCalendar.monthScrollListener = {
            binding.monthYearTitle.text = titleFormatter.format(it.yearMonth)

            // Select the first day of the month when
            // we scroll to a new month.
            selectDate(it.yearMonth.atDay(1))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = TimeSlotMonthCalendarHeaderBinding.bind(view).legendLayout.root
        }
        binding.exThreeCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
                        when (nightModeFlags) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                tv.setTextColorRes(R.color.white)
                            }
                            Configuration.UI_MODE_NIGHT_NO -> { tv.setTextColorRes(R.color.opaque_black) }
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> { tv.setTextColorRes(R.color.opaque_black) }
                        }

                    }
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.exThreeCalendar.notifyDateChanged(it) }
            binding.exThreeCalendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }


    private fun refreshDate(date: LocalDate) {
        binding.exThreeCalendar.notifyDateChanged(date)
        //updateAdapterForDate(date)
    }

    private fun deleteEvent(event: Event) {
        /*val date = event.date
        events[date] = events[date].orEmpty().minus(event)
        updateAdapterForDate(date)*/
        //qui ci va la navigazione al time slot info, se realmente vogliamo implementarla
    }

    @SuppressLint("NotifyDataSetChanged") //nessuna altra alternativa disponibile
    private fun updateAdapterForDate(date: LocalDate) {
        eventsAdapter.apply {
            events.clear()
            events.addAll(this@TimeSlotMonthCalendar.events[date].orEmpty())
            notifyDataSetChanged()
        }
        binding.exThreeSelectedDateText.text = selectionFormatter.format(date)
    }

    override fun onDetach() {
        vm.clearTimeSlots()
        vm.setIsEmptyFlag(false)
        //vm.justUpdated = false
        super.onDetach()
    }
}

private fun View.makeVisible() {
    visibility = View.VISIBLE
}

private fun View.makeInVisible() {
    visibility = View.INVISIBLE
}

internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return context.layoutInflater.inflate(layoutRes, this, attachToRoot)
}

internal val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }
    return daysOfWeek
}