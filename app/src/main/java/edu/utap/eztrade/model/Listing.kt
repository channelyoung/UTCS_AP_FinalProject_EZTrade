package edu.utap.eztrade.model

import android.graphics.Color
import android.location.Location
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.text.clearSpans
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName

// Firebase insists we have a no argument constructor
data class Listing(
    // Auth information
    var name: String = "",
    var ownerUid: String = "",
    // Listing information
    var title: String = "",
    var detail: String = "",
    var latitude: Float = 0.0f,
    var longitude: Float = 0.0f,
    var pictureUUIDs: List<String> = listOf(),
    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,
    // listingID is generated by firestore, used as primary key
    var listingID: String = ""
)