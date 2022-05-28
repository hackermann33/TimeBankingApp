package it.polito.timebankingapp.ui.reviews.addreview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import it.polito.timebankingapp.R


class AddReviewFragment : Fragment(R.layout.fragment_add_review) {

    private lateinit var v : View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
    }

}