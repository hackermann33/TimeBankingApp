package it.polito.timebankingapp.ui.reviews

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.review.Review
import it.polito.timebankingapp.model.timeslot.TimeSlot

class ReviewsViewModel(application: Application): AndroidViewModel(application) {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private var _alreadyReviewed = MutableLiveData<Boolean>()
    val alreadyReviewed: LiveData<Boolean> = _alreadyReviewed

    private lateinit var l: ListenerRegistration

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addReview(review: Review, reviewedUserId: String){
        // versione vecchia senza check sulla esistenza
        val newReviewRef = db.collection("reviews_test")
            .document(reviewedUserId).collection("userReviews").document()

        newReviewRef.set(review).addOnSuccessListener{
            Log.d("reviews_add","Successfully added")
        }.addOnFailureListener{ Log.d("reviews_add", "Error on adding")}

/*      //versione con check da debuggare e ripensare
        val newReviewRef = db.collection("reviews_test")
            .document(reviewedUserId).collection("userReviews").document()

        val checkReviewRef = db.collection("reviews_test")
            .document(reviewedUserId).collection("userReviews").whereEqualTo("reviewer.id",Firebase.auth.uid.toString())

        checkReviewRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (!document.isEmpty) {
                    newReviewRef.set(review).addOnSuccessListener{
                        Log.d("reviews_add","Successfully added")
                    }.addOnFailureListener{ Log.d("reviews_add", "Error on adding")}
                }
            } else {
                Log.d("reviews_add", "Failed with: ", task.exception)
            }
        }*/
    }

    fun checkIfAlreadyReviewed(reviewedUserId: String, role: String, reviewedTimeSlotId: String){
        val checkReviewRef = db.collection("reviews_test")
            .document(reviewedUserId/*.plus("_").plus(role)*/).collection("userReviews")
            .whereEqualTo("reviewer.id",Firebase.auth.uid.toString()).whereEqualTo("reviewedTimeSlotId", reviewedTimeSlotId)

        checkReviewRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                _alreadyReviewed.value = !document.isEmpty //esiste già la recensione
            } else {
                Log.d("reviews_check", "Failed with: ", task.exception)
            }
        }
    }
    /*
    fun retrieveRequesterInfo(timeSlot: TimeSlot){
        val checkReviewRef = db.collection("reviews_test")
            .document(reviewedUserId).collection("userReviews")
            .whereEqualTo("reviewer.id",Firebase.auth.uid.toString())

        checkReviewRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                _alreadyReviewed.value = !document.isEmpty
            } else {
                Log.d("reviews_check", "Failed with: ", task.exception)
            }
        }
    }*/

    fun retrieveAllReviews(userId: String) {
        //to-do
        l = db.collection("reviews_test").document(userId)
            .collection("userReviews").addSnapshotListener {
                v,e ->
            if(e == null){
                val temp = v!!.mapNotNull {
                        d -> d.toObject<Review>()
                }
                _reviews.value = temp
            }else {
                _reviews.value = emptyList()
            }
        }
    }
}