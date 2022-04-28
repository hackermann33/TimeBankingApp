package it.polito.timebankingapp.model.timeslot

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "time_slots", indices = [Index("title")])
class TimeSlot(
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var title: String = ""
    var description: String = ""
    var date: String = ""
    var time: String = ""
    var duration: String = ""
    var location: String = ""
    var restrictions: String = ""



    override fun toString(): String = "{ title:$title, description: $description, date: $date, time: $time, duration: $duration, location: $location"

    fun isValid() : Boolean{
        return title != "" && description != "" && date != "" && time != "" && duration != "" && location != "" && restrictions != ""
    }

    fun clone(ts : TimeSlot){
        this.title = ts.title
        this.description = ts.description
        this.date = ts.date
        this.time = ts.time
        this.duration = ts.duration
        this.location = ts.location
        this.restrictions = ts.restrictions
    }
    /*Here, I'm not checking that String is not empty, because if it's empty it will be used default image*/
    /*fun isValid(): Boolean {
        return fullName.isNotEmpty() && nick.isNotEmpty() && isValidEmail() && location.isNotEmpty() && description.isNotEmpty()
    }

    private fun isValidEmail(): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }*/
    fun getDay(): Int {
        return date.split("/")[0].trim().toInt()
    }
    fun getMonth(): Int {
        return date.split("/")[1].trim().toInt()
    }
    fun getYear(): Int {
        return date.split("/")[2].trim().toInt()
    }
    fun getHour(): Int {
        return time.split(":")[0].trim().toInt()
    }
    fun getMinute(): Int {
        return time.split(":")[1].trim().toInt()
    }

    fun getCalendar(): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        if(date.isNotEmpty()) {
            calendar[Calendar.YEAR] = getYear()
            calendar[Calendar.MONTH] = getMonth()
            calendar[Calendar.DAY_OF_MONTH] = getDay()
            calendar[Calendar.HOUR] = getHour()
            calendar[Calendar.MINUTE] = getMinute()

        }
        return calendar
    }
}

