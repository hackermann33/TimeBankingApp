package it.polito.timebankingapp.model

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.model.chat.ChatsListItem
import it.polito.timebankingapp.model.user.User
import java.text.SimpleDateFormat
import java.util.*

class Helper {
    companion object {
        fun loadImageIntoView(view: CircleImageView, url: String) {
            val storageReference = FirebaseStorage.getInstance().reference
            if (url.isEmpty()) return
            val picRef = storageReference.child(url)

            val circularProgressDrawable = CircularProgressDrawable(view.context)

            /*circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f*/
            circularProgressDrawable.start()

            picRef.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    Glide.with(view.context)
                        .load(downloadUrl)
                        .placeholder(circularProgressDrawable)
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
            return if(req.offerer.id == Firebase.auth.uid) Request.TYPE_REQUEST_CHAT else Request.TYPE_OFFER_CHAT
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