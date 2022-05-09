package it.polito.timebankingapp.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.user.User

class ProfileViewModel(application: Application) : AndroidViewModel(application) {


    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _fireBaseUser = MutableLiveData<FirebaseUser?>(Firebase.auth.currentUser)
    val fireBaseUser: LiveData<FirebaseUser?> = _fireBaseUser


    private lateinit var l: ListenerRegistration

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        if (fireBaseUser.value != null) {
            registerListener()
        }
    }

    fun registerListener() {
        var usr: User
        l = db.collection("users").document(fireBaseUser.value!!.uid)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    /* Documento appena creato */
                    if (v != null) {
                        if(!v.exists()) {
                            usr = User().also { it.id = fireBaseUser.value!!.uid; it.fullName =
                                fireBaseUser.value!!.displayName!!; it.email = fireBaseUser.value!!.email!!
                            }
                            db.collection("users").document(fireBaseUser.value!!.uid)
                                .set(usr)
                            _user.value = usr!!
                        }
                        /* Documento già esistente */
                        else {
                            usr = v.toUser()!!
                            _user.value = usr!!
                        }
                    }
                } else _user.value = User()
            }

    }

    fun logIn(user: FirebaseUser) {
        assert(user == Firebase.auth.currentUser)
        _fireBaseUser.value = user
        registerListener()
    }

    fun logOut() {
        _fireBaseUser.value = Firebase.auth.currentUser

    }

    override fun onCleared() {
        super.onCleared()
        l.remove()
    }

    private fun DocumentSnapshot.toUser(): User? {

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

    fun editUser(usr: User) {
        db.collection("users").document(usr.id).set(usr)

    }


}

