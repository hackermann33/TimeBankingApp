package it.polito.timebankingapp

import android.text.TextUtils
import android.util.Patterns
import java.io.Serializable


class User(var pic : String?,
           var fullName: String = "",
           var nick: String = "",
           var email: String = "",
           var location: String = "",
           var skills: MutableList<String> = mutableListOf(),
           var description: String = "",
           var balance: Int = 0
    ) : Serializable {

    fun isGood(): Boolean {
        return (pic?.isNotEmpty() ?: false) && fullName.isNotEmpty() && nick.isNotEmpty() && isValidEmail() && location.isNotEmpty() && description.isNotEmpty()
    }

    private fun isValidEmail(): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    /*{ }*/
}