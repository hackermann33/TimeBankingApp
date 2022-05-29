package it.polito.timebankingapp.model.user

import android.text.TextUtils
import android.util.Patterns
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import java.io.Serializable


data class User(
    var id: String = "",
    var profilePicUrl: String = "",
    var fullName: String = "",
    var nick: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var balance: Int = 0,
    var skills: MutableList<String> = mutableListOf(),
) : Serializable {

    /*Here, I'm not checking that String is not empty, because if it's empty it will be used default image*/
    @Exclude
    fun isValid(): Boolean {
        return (fullName.isNotEmpty() && fullName.length <= 45)
                && (nick.isNotEmpty() && nick.length <= 20)
                && (isValidEmail() && email.length <= 45)
                && (location.isNotEmpty() && location.length <= 50)
                && (description.isNotEmpty() && description.length <= 200)
    }

    @Exclude
    private fun isValidEmail(): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }


    fun hasImage() = profilePicUrl.isNotEmpty()

}


