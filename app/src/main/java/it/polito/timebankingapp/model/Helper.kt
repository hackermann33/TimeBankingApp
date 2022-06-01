package it.polito.timebankingapp.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.review.Review
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.model.user.User
import java.lang.IllegalStateException
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

        /* If the current user is the requester, the chat will be a chat to an offer*/
        fun getType(cli: Chat): Int = if (cli.requestId== Firebase.auth.uid) Chat.CHAT_TYPE_TO_OFFERER else Chat.CHAT_TYPE_TO_REQUESTER


        fun getOtherUser(req: Chat): CompactUser {
            return if(req.getType() == Chat.CHAT_TYPE_TO_OFFERER)
                req.offerer
            else
                req.requester
        }

        fun isYesterday(d: Date): Boolean {
            return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
        }

        const val KEY_PROFILE_PIC_URL = "profilePicUrl"
        const val KEY_FULL_NAME = "fullName"
        const val KEY_NICK = "nick"
        const val KEY_EMAIL = "email"
        const val KEY_LOCATION = "location"
        const val KEY_DESCRIPTION = "description"
        const val KEY_BALANCE = "balance"
        const val KEY_SKILLS = "skills"
        const val KEY_REVIEWS = "reviews"


        /*fun DocumentSnapshot.toUser(): User? {
            return try {
                val pic = get(KEY_PROFILE_PIC_URL) as String
                val fullName = get(KEY_FULL_NAME) as String
                val nick = get(KEY_NICK) as String
                val email = get(KEY_EMAIL) as String
                val location = get(KEY_LOCATION) as String
                val desc = get(KEY_DESCRIPTION) as String
                val balance = get(KEY_BALANCE) as Long
                val skills = get(KEY_SKILLS) as MutableList<String>
                val reviews = get(KEY_REVIEWS) as MutableList<Any>

                User(id, pic, fullName, nick, email, location, desc, balance.toInt(), skills, reviews.map { it as Review }.toMutableList())
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "except : {$e}")
                null
            }
        }*/

        fun makeRequestId(timeSlotId: String, requesterId: String): String{
            return timeSlotId + "_" + requesterId
        }

        fun extractRequesterId(requestId: String): String {
            return requestId.split("_").last()
        }

        fun getChatType(req: Chat): Int {
            return if(req.offerer.id == Firebase.auth.uid) Chat.CHAT_TYPE_TO_REQUESTER else Chat.CHAT_TYPE_TO_OFFERER
        }

        fun fromRequestToChat(r: Chat): Chat {
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

        fun requestMessage(cli: Chat): String {
            return "Hi ${cli.offerer.nick}, I'm ${cli.requester.nick} and I'm interested" +
                    " to your service (${cli.timeSlot.title})"

        }

        fun requestMessage(offNick : String, tsTitle: String): String {
            return "Hi ${offNick}, I'm ${Firebase.auth.uid} and I'm interested" +
                    " to your service (${tsTitle})"
        }

        /*fun compactUserFromUser(user: User) : CompactUser {
            var asOffAvg : Double = 0.0
            for(r in user.asOffererReviews)
                asOffAvg += r.stars
            asOffAvg /= user.asOffererReviews.size

            var asReqAvg : Double = 0.0
            for(r in user.asRequesterReviews)
                asReqAvg += r.stars
            asReqAvg /= user.asRequesterReviews.size

            return CompactUser(
                user.id,
                user.profilePicUrl,
                user.nick,
                user.location,
                CompactReview(asOffAvg, user.asOffererReviews.size),
                CompactReview(asReqAvg, user.asRequesterReviews.size),
                user.asOffererReviews.size + user.asRequesterReviews.size,
                user.balance
                )
        }*/

        fun setConfirmationOnButton(context: Context, btn: Button) {
            btn.isEnabled = false
            val drawCheckedIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_24)
            btn.setCompoundDrawablesWithIntrinsicBounds(null,null,drawCheckedIcon, null)
        }

        fun getReviewType(timeSlot: TimeSlot): Int {
            if(timeSlot.offerer.id == Firebase.auth.uid)
                return Review.AS_OFFERER_TYPE
            else if(timeSlot.assignedTo.id == Firebase.auth.uid)
                return Review.AS_REQUESTER_TYPE
            else{
                throw IllegalStateException("Something is wrong..")
            }
        }

        fun getReviewer(timeSlot: TimeSlot): CompactUser {
            if(timeSlot.offerer.id == Firebase.auth.uid)
                return timeSlot.offerer
            else if(timeSlot.assignedTo.id == Firebase.auth.uid)
                return timeSlot.assignedTo
            else{
                throw IllegalStateException("Something is wrong..")
            }
        }




        fun extractTimeSlotId(requestId: String): String {
            return requestId.split("_").first()

        }

        fun getUserToReview(timeSlot: TimeSlot): CompactUser {
            if(timeSlot.offerer.id == Firebase.auth.uid)
                return timeSlot.assignedTo
            else if(timeSlot.assignedTo.id == Firebase.auth.uid)
                return timeSlot.offerer
            else{
                throw IllegalStateException("Something is wrong..")
            }
        }


    }

    }