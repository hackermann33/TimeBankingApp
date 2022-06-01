package it.polito.timebankingapp.model.user

import android.text.TextUtils
import android.util.Patterns
import com.google.firebase.firestore.Exclude
import it.polito.timebankingapp.model.review.Review
import java.io.Serializable


data class User(
    var id: String = "",
    var profilePicUrl: String = "",
    var fullName: String = "",
    var nick: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var balance: Int = 0,
    var skills: MutableList<String> = mutableListOf(),
    var reviews: MutableList<Review> = mutableListOf(),
) : Serializable {

    /*Here, I'm not checking that String is not empty, because if it's empty it will be used default image*/
    @Exclude
    fun isValid(): Boolean {
        return (fullName.isNotEmpty() && fullName.length <= 45)
                && (nick.isNotEmpty() && nick.length <= 20)
                && (isValidEmail() && email.length <= 45)
                && (location.isNotEmpty() && location.length <= 50)
                && (description.isNotEmpty() && description.length <= 200)
    }

    @Exclude
    private fun isValidEmail(): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }


    fun getReviews(reviewType:Int) =  reviews.filter{it.type==reviewType}

    fun getReviewsScore(reviewType: Int): Double = getReviews(reviewType).map { it.stars }.average().also { if(it.isNaN()) return 0.0}

    fun hasImage() = profilePicUrl.isNotEmpty()

    fun toCompactUser(): CompactUser {
        val cr = CompactReview(getReviewsScore(Review.AS_OFFERER_TYPE), getReviews(Review.AS_OFFERER_TYPE).size)
        val cr2 = CompactReview(getReviewsScore(Review.AS_REQUESTER_TYPE), getReviews(Review.AS_REQUESTER_TYPE).size)

        return CompactUser(
            id, profilePicUrl, nick, location, cr, cr2, balance = balance)
    }

}


