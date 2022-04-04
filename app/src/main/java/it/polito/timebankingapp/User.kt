package it.polito.timebankingapp

import android.text.TextUtils
import android.util.Patterns
import java.io.Serializable


class User(var pic : String = "",
           var fullName: String = "",
           var nick: String = "",
           var email: String = "",
           var location: String = "",
           var skills: MutableList<String> = mutableListOf(),
           var description: String = "",
           var balance: Int = 0
    ) : Serializable {

    /*Here, I'm not checking that String is not empty, because if it's empty it will be used default image*/
    fun isValid(): Boolean {
        return fullName.isNotEmpty() && nick.isNotEmpty() && isValidEmail() && location.isNotEmpty() && description.isNotEmpty()
    }

    private fun isValidEmail(): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
}