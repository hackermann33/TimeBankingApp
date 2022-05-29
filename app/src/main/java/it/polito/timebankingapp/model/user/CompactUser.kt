package it.polito.timebankingapp.model.user


/* Class needed to hadle user infos inside a chat/request */
data class CompactUser (
    var id: String = "",
    var profilePicUrl: String = "",
    var nick: String = "",
    var location: String = "",
    var asOffererReview: CompactReview = CompactReview(),
    var asRequesterReview: CompactReview = CompactReview(),
    var nReviews: Int = 0,
    var balance: Int = 0,
){}

data class CompactReview (
    //var id: String
    var score: Double = 0.0,
    var number: Int  = 0,
)