package it.polito.timebankingapp.model.timeslot

import android.util.Log
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class TimeSlot(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var time: String = "",
    var duration: String = "",
    var location: String = "",
    var restrictions: String = "",
    var relatedSkill: String = ""
) : Serializable {

    override fun toString(): String = "{ title:$title, description: $description, date: $date, time: $time, duration: $duration, location: $location"

    @Exclude
    fun isValid() : Boolean{

        val todayDate = LocalDate.now()
        val fields = date.split("/")
        val thisDate = LocalDate.of(fields[2].toInt(),fields[1].toInt(),fields[0].toInt())
        val dateFlag = !thisDate.isBefore(todayDate)

        var durationFlag = false
        if (duration.length <= 2){
            if(duration.toInt() < 24)
                durationFlag = true
        }

        return (title != "" && title.length <= 30)
                && (description != "" && description.length <= 200)
                && (date != "" && dateFlag)
                && time != ""
                && (duration != "" && durationFlag)
                && (location != "" && location.length <= 50)
                && (restrictions != "" && restrictions.length <= 100 )
                && (relatedSkill != "" && relatedSkill.length <= 30)
    }

    @Exclude
    fun getCalendar(): Calendar {
        val cal = Calendar.getInstance(TimeZone.getDefault())


        if(date.isNotEmpty() && time.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            cal.time = sdf.parse(this.date + " " + this.time) as Date // all done
        }

        Log.d("getCalendar", cal.timeInMillis.toString())
        return cal
    }

}

