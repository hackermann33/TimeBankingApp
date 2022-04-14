package it.polito.timebankingapp.model.timeslot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

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



    override fun toString(): String = "{ title:$title, description: $description, date: $date, time: $time, duration: $duration, location: $location"

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
}

