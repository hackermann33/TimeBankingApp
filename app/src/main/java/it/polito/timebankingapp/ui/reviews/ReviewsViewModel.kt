package it.polito.timebankingapp.ui.reviews

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.timebankingapp.model.review.Review

class ReviewsViewModel(application: Application): AndroidViewModel(application) {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    fun addReview(){
        //to-do
    }

    fun retrieveReviews() {
        //to-do
    }
}