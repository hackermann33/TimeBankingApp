package it.polito.timebankingapp.model

import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.model.user.User
import java.text.SimpleDateFormat
import java.util.*

class Helper {
    companion object {
        const val TAG = "Helper"


        fun loadImageIntoView(view: ImageView, progressBar: ProgressBar, url: String) {
            val storageReference = FirebaseStorage.getInstance().reference
            if (url.isEmpty()){
                progressBar.visibility = View.GONE;
                return
            }
            val picRef = storageReference.child(url)


            picRef.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    Glide.with(view.context)
                        .load(downloadUrl).circleCrop().listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                p0: GlideException?,
                                p1: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                p3: Boolean
                            ): Boolean {
                                Log.e(TAG, "onLoadFailed")
                                //do something if error loading
                                return false
                            }
                            override fun onResourceReady(
                                p0: Drawable?,
                                p1: Any?,
                                target: Target<Drawable>?,
                                p3: DataSource?,
                                p4: Boolean
                            ): Boolean {
                                Log.d(TAG, "OnResourceReady")
                                //do something when picture already loaded
                                progressBar.visibility = View.GONE
                                return false
                            }
                        }).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
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

        fun getOtherUser(req: ChatsListItem): CompactUser {
            return if(req.type == ChatsListItem.CHAT_TYPE_TO_OFFERER)
                req.offerer
            else
                req.requester
        }

        fun isYesterday(d: Date): Boolean {
            return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
        }

        const val KEY_PROFILE_PIC_URL = "profilePicUrl"
        const val KEY_PROFILE_PIC_FULL_NAME = "fullName"
        const val KEY_PROFILE_PIC_NICK = "nick"
        const val KEY_PROFILE_PIC_EMAIL = "email"
        const val KEY_PROFILE_PIC_LOCATION = "location"
        const val KEY_PROFILE_PIC_DESCRIPTION = "description"
        const val KEY_PROFILE_PIC_BALANCE = "balance"
        const val KEY_PROFILE_PIC_SKILLS = "skills"


        fun DocumentSnapshot.toUser(): User? {
            return try {
                val pic = get(KEY_PROFILE_PIC_URL) as String
                val fullName = get(KEY_PROFILE_PIC_FULL_NAME) as String
                val nick = get(KEY_PROFILE_PIC_NICK) as String
                val email = get(KEY_PROFILE_PIC_EMAIL) as String
                val location = get(KEY_PROFILE_PIC_LOCATION) as String
                val desc = get(KEY_PROFILE_PIC_DESCRIPTION) as String
                val balance = get(KEY_PROFILE_PIC_BALANCE) as Long
                val skills = get(KEY_PROFILE_PIC_SKILLS) as MutableList<String>

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

        fun getChatType(req: ChatsListItem): Int {
            return if(req.offerer.id == Firebase.auth.uid) ChatsListItem.CHAT_TYPE_TO_REQUESTER else ChatsListItem.CHAT_TYPE_TO_OFFERER
        }

        fun fromRequestToChat(r: ChatsListItem): ChatsListItem {
            return r // to remove
        }




        fun dateToDisplayString(d: Date): String {
            var pattern: String = when {
                Helper.isYesterday(d) -> return "yesterday"
                DateUtils.isToday(d.time) -> "HH:mm"
                else -> "dd/MM/yy"
            }

            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            return sdf.format(d)

        }
    }

    }