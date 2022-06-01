package it.polito.timebankingapp.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.user.User
import java.io.ByteArrayOutputStream
import java.util.*


class ProfileViewModel(application: Application) : AndroidViewModel(application) {


    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _timeslotUser = MutableLiveData<User>()
    val timeslotUser: LiveData<User> = _timeslotUser


    /* maybe this, can be removed*/
    private val _fireBaseUser = MutableLiveData<FirebaseUser?>(Firebase.auth.currentUser)
    val fireBaseUser: LiveData<FirebaseUser?> = _fireBaseUser

    private lateinit var l: ListenerRegistration

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                _fireBaseUser.value = it.currentUser
                l = registerCurrentUserListener()
            } else if(::l.isInitialized)
                l.remove()
        }
        //_timeslotUserImageLoading.value = true
    }

    private fun registerCurrentUserListener(): ListenerRegistration {
        var usr: User
        val lTmp = db.collection("users").document(fireBaseUser.value!!.uid)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    if (v != null) {
                        /* Utente appena creato */
                        if (!v.exists()) {
                            usr = User().also {
                                it.id = fireBaseUser.value!!.uid; it.fullName =
                                fireBaseUser.value!!.displayName!!; it.email =
                                fireBaseUser.value!!.email!!; it.balance = 3
                            }
                            db.collection("users").document(fireBaseUser.value!!.uid)
                                .set(usr)
                            _user.value = usr
                        }
                        /* Documento già esistente */
                        else {
                            usr = v.toObject<User>()!! //If you get an exception here, check that pic field is profilePicUrl
                            _user.value = usr
                        }
                    }
                } else _user.value = User()
            }
        return lTmp
    }

    /* Glide substituted it*/
    /*private fun downloadProfileImage() {
        val storageRef = FirebaseStorage.getInstance().reference

        if (user.value!!.profilePicUrl.isNotEmpty()) {

            storageRef.child(user.value!!.profilePicUrl).downloadUrl.addOnSuccessListener {
                Log.d("getProfileImage", "usrId: ${user.value}")
                val picRef = storageRef.child(it.lastPathSegment.toString())
                val size: Long = 2 * 1024 * 1024
                picRef.getBytes(size).addOnSuccessListener {

                    _userImage.postValue(BitmapFactory.decodeByteArray(it, 0, it.size))
                    // Data for "images/island.jpg" is returned, use this as needed
                }.addOnFailureListener {
                    // This should not happen since we check before if the image exists.
                    _userImage.postValue(null)
                    Log.d("getProfileImage", "usr: ${user.value.toString()} \npicRef: $picRef")
                    Log.d("getProfileImage", it.toString())
                }
            }.addOnFailureListener {
                Log.d("downloadProfileImage", it.toString())
                *//* This means that the user exists but it hasn't set an image yet *//*
                _userImage.postValue(null)
            }

        } else {
            Log.d("downlodProfileImage", "This should never happen... User: ${user.value}")
        }
    }*/


    fun logIn(user: FirebaseUser) {
        assert(user == Firebase.auth.currentUser)
    }

    fun logOut() {
        _fireBaseUser.value = null
        _user.postValue(User())
    }

    override fun onCleared() {
        super.onCleared()
        l.remove()
    }


    fun editUser(usr: User) {

        val srcRef = db.collection("users").document(usr.id)
        /*
        Manage multiple updates !!!
        val otherRef = db.collection("requests").whereArrayContains("users", usr.id).

        db.runBatch{
            batch ->
            db.collection("users").document(usr.id).set(usr)

        }*/

    }


    fun editUserImage(imageBitmap: Bitmap?) {

        if(imageBitmap == null) return

        // Create file metadata including the content type
        val metadata = storageMetadata {
            contentType = "image/jpg"
        }

        /*By bitmap*/
        val baos = ByteArrayOutputStream()
        if (imageBitmap.byteCount > 1024 * 1024) {
            val resizedImageBitmap = getResizedBitmap(imageBitmap, 1024)
            resizedImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        } else
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()

        if(user.value!!.profilePicUrl.isEmpty())
            _user.value = user.value!!.copy(profilePicUrl = "images/".plus(UUID.randomUUID().toString()))

        // Upload the file and metadata
        FirebaseStorage.getInstance().reference.child("${user.value?.profilePicUrl}").putBytes(data, metadata)
            .addOnSuccessListener {
                _user.value = user.value!!.copy()
                Log.d("editUserImage", "success: $it")
            }.addOnFailureListener {
            Log.d("editUserImage", "failure: $it")
        }

    }

    fun getUserFromId(userId: String): Task<DocumentSnapshot> {
        return db.collection("users").document(userId).get()
    }

    fun retrieveTimeSlotProfileData(userId: String) {
        var timeslotUsr: User
        l = db.collection("users").document(userId)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    if (v != null) {
                        timeslotUsr = v.toObject<User>()!!
                        _timeslotUser.value = timeslotUsr
                    }

                } else _timeslotUser.value = User()
            }
    }

    fun updateCurrentUser() {
        TODO("Not yet implemented")
    }

    fun setTimeSlotUser(toUser: User) {
        _timeslotUser.value = toUser
    }
}

fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
    var width = image.width
    var height = image.height
    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 1) {
        width = maxSize
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxSize
        width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(image, width, height, true)
}


