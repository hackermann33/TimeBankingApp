package it.polito.timebankingapp.model

import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.model.user.User

class Helper {
    companion object {
        fun loadImageIntoView(view: CircleImageView, url: String) {
            val storageReference = FirebaseStorage.getInstance().reference
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
    }
}