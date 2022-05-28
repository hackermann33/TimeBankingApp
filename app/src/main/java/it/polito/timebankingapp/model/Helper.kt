package it.polito.timebankingapp.model

import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.chat.ChatsListItem
import it.polito.timebankingapp.model.user.User
import java.text.SimpleDateFormat
import java.util.*

class Helper {
    companion object {
        const val TAG = "Helper"

        fun loadImageIntoView(view: CircleImageView, progressBar: ProgressBar, url: String) {
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
                        .load(downloadUrl).listener(object : RequestListener<Drawable> {
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

        fun getChatType(req: Request): Int {
            return if(req.offerer.id == Firebase.auth.uid) Request.CHAT_TYPE_TO_REQUESTER else Request.CHAT_TYPE_TO_OFFERER
        }

        fun fromRequestToChat(r: Request): ChatsListItem {
            val otherUser = Helper.getOtherUser(r)
            val timeStr = r.lastMessageTime.toDisplayString()
            val userId = Firebase.auth.uid.toString()
            return ChatsListItem(r.requestId, userId,  r.timeSlot.id, r.timeSlot.title,  otherUser.fullName, otherUser.pic,
                4.5f, 6,  r.lastMessageText, timeStr, r.unreadMsg, 0, r.status, Helper.getChatType(r))
        }

        private fun Date.toDisplayString(): String {

            var pattern: String = when {
                Helper.isYesterday(this) -> return "yesterday"
                DateUtils.isToday(this.time) -> "HH:mm"
                else -> "dd/MM/yy"
            }

            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            return sdf.format(this)

        }
    }

    }