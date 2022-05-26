package it.polito.timebankingapp.model.user

import android.text.TextUtils
import android.util.Patterns
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable


class User(
    var id: String = "",
    var pic: String = "",
    var fullName: String = "",
    var nick: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var balance: Int = 0,
    var skills: MutableList<String> = mutableListOf(),
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

    fun hasImage() = pic.isNotEmpty()



    fun DocumentSnapshot.toUser(): User? {

        return try {
            val pic = get("pic") as String
            val fullName = get("fullName") as String
            val nick = get("nick") as String
            val email = get("email") as String
            val location = get("location") as String
            val desc = get("description") as String
            val balance = get("balance") as Long
            val skills = get("skills") as MutableList<String>

            User(id, pic, fullName, nick, email, location, desc, balance.toInt(), skills)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


