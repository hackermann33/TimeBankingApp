package it.polito.timebankingapp.model

import android.text.format.DateUtils
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.model.user.User
import java.util.*

class Helper {
    companion object {
        fun loadImageIntoView(view: CircleImageView, url: String) {
            val storageReference = FirebaseStorage.getInstance().reference
            if (url.isEmpty()) return
            val picRef = storageReference.child(url)

            picRef.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    Glide.with(view.context)
                        .load(downloadUrl)
                        .into(view)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        "loadImage",
                        "Getting download url was not successful.",
                        e
                    )
                }
        }

        fun getOtherUser(req: Request): User {
            return if(req.offerer.id == Firebase.auth.uid)
                req.requester
            else
                req.offerer ;
        }

        fun isYesterday(d: Date): Boolean {
            return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
        }

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

        fun makeRequestId(timeSlotId: String, requesterId: String): String{
            return timeSlotId + "_" + requesterId
        }

        fun extractRequesterId(requestId: String): String {
            return requestId.split("_").last()
        }

    }
}