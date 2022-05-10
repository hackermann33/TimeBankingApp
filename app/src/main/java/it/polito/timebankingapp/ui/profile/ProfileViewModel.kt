package it.polito.timebankingapp.ui.profile

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.model.user.User
import java.io.File
import java.io.FileInputStream
import java.util.*


/* Test new branch*/

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
                    if (v != null) {
                        /* Documento appena creato */
                        if(!v.exists()) {
                            usr = User().also { it.id = fireBaseUser.value!!.uid; it.fullName =
                                fireBaseUser.value!!.displayName!!; it.email = fireBaseUser.value!!.email!!;
                                it.pic = "images/".plus(UUID.randomUUID().toString());
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
        _fireBaseUser.value = null
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

    fun editUser(usr: User, path: String) {
        // Create a storage reference from our app
        val storage: FirebaseStorage = FirebaseStorage.getInstance();
        val storageRef = storage.reference

// Create a reference to 'images/mountains.jpg'
        //val imageName = "images/".plus(UUID.randomUUID().toString())
        val profilePicRef = storageRef.child(usr.pic); //substitute image or create new one
        val file = Uri.fromFile(File(path))
        val uploadTask = profilePicRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            it.stackTrace
        }/*.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            taskSnapshot.toString()
        }*/

       /* val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            profilePicRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
            } else {
                // Handle failures
                // ...
            }
        }*/

        //usr.pic = imageName

        db.collection("users").document(usr.id).set(usr)

    }

    fun retrieveProfilePic(usr: User, profilePic:CircleImageView){
        val storage: FirebaseStorage = FirebaseStorage.getInstance();
        //val storageRef = storage.reference

        // Create a reference to a file from a Google Cloud Storage URI
        val gsReference = storage.getReferenceFromUrl("gs://timebankingdb.appspot.com/".plus(usr.pic))

        /*val TWO_MEGABYTES: Long = 2 * 1024 * 1024
        gsReference.getBytes(TWO_MEGABYTES).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            profilePic.setImageBitmap(bitmap)
        }.addOnFailureListener {
            it.stackTrace
        }*/

        val localFile = File.createTempFile("profile", "jpg",File("/data/user/0/it.polito.timebankingapp/app_imageDir"))

        gsReference.getFile(localFile).addOnSuccessListener {
            // Local temp file has been created
            val bitmap = BitmapFactory.decodeStream(FileInputStream(localFile))
            profilePic.setImageBitmap(bitmap)
        }.addOnFailureListener {
            // Handle any errors
        }
    }

}

