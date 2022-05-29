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

    private lateinit var l: ListenerRegistration

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addReview(review: Review){
        val newReviewRef = db.collection("reviews_test")
            .document(review.reviewer.getValue("id")).collection("userReviews").document()

        newReviewRef.set(review).addOnSuccessListener{
            Log.d("timeSlots_add","Successfully added")
        }.addOnFailureListener{ Log.d("timeSlots_add", "Error on adding")}
    }

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