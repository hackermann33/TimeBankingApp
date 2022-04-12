package it.polito.timebankingapp.ui.timeslot_details

import android.text.TextUtils
import android.util.Patterns
import java.io.Serializable


class TimeSlot(var title : String = "",
               var description: String = "",
               var date: String = "",
               var time: String = "",
               var duration: String = "",
               var location: String = ""
    ) : Serializable {

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