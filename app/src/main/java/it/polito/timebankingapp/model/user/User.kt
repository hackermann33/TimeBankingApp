package it.polito.timebankingapp.model.user

import android.text.TextUtils
import android.util.Patterns
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

@Entity(tableName = "users", indices = [Index("nick")])
class User(@PrimaryKey(autoGenerate = true)
           var id: Int = 0,
           var pic : String = "",
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