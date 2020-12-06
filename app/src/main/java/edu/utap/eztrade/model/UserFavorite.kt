package edu.utap.eztrade.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

// Firebase insists we have a no argument constructor
data class UserFavorite(
    // Auth information
    var ownerUid: String = "",
    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,
    var listingID: String = ""
)