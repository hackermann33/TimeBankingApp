package it.polito.timebankingapp.ui.reviews

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.review.Review

class ReviewsListFragment : Fragment(R.layout.fragment_reviews_list) {
    private lateinit var rv : RecyclerView
    private lateinit var adTmp: ReviewsViewAdapter
    private lateinit var v : View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        rv = view.findViewById(R.id.reviews_list_rv)
        rv.layoutManager = LinearLayoutManager(context)

        val review = Review()
        val tempReviewsList = mutableListOf<Review>()

        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)
        tempReviewsList.add(review)


        adTmp = ReviewsViewAdapter(tempReviewsList)
        rv.adapter = adTmp
    }
}