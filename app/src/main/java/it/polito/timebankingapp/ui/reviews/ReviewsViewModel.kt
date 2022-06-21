package it.polito.timebankingapp.ui.reviews

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.review.Review
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel

class ReviewsViewModel(application: Application): AndroidViewModel(application) {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private var _alreadyReviewed = MutableLiveData<Boolean>()
    val alreadyReviewed: LiveData<Boolean> = _alreadyReviewed

    private lateinit var l: ListenerRegistration


    private val _review = MutableLiveData<Review>()
    val review: LiveData<Review> = _review

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addReview(review: Review){
        val userDocRef = db.collection("users").document(review.userToReview.id)
        val offererRef = db.collection("requests").whereEqualTo("offerer.id", review.userToReview.id)
        val requesterRef = db.collection("requests").whereEqualTo("requester.id", review.userToReview.id)

        var updatedCompactUser: CompactUser
        userDocRef.update(mapOf("reviews" to FieldValue.arrayUnion(review.copy(published =true)))).addOnSuccessListener {
            Log.d("reviews_add","Successfully added")
            db.runTransaction {
                    transaction ->

                    updatedCompactUser = transaction.get(userDocRef).toObject<User>()?.toCompactUser() ?: CompactUser()

                offererRef.get().addOnSuccessListener {
                    it.forEach { transaction.update(it.reference, "offerer",  updatedCompactUser, "timeSlot.offerer", updatedCompactUser)
                    }
                }
                requesterRef.get().addOnSuccessListener {
                    it.forEach { transaction.update(it.reference, "requester",  updatedCompactUser, "timeSlot.requester", updatedCompactUser)
                    }
                }

            }

        }.addOnFailureListener  { Log.d("reviews_add", "Error on adding") }


        /* Update review references in CompactUser */


    }

    fun checkIfAlreadyReviewed(timeSlot: TimeSlot, reviewer: User, userToReview: User): Review?{
        Log.d("checkIfAlready..", "userToReview:$userToReview\n reviewer:$reviewer")

        return userToReview.reviews.find{r -> r.referredTimeSlotId == timeSlot.id && r.reviewer.id == reviewer.id}
    }


    /* Set the review to review inside vm*/
    fun setReview(rev: Review) {
        _review.postValue(rev)
    }
}