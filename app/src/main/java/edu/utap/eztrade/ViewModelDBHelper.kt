package edu.utap.eztrade

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.utap.eztrade.model.Listing
import edu.utap.eztrade.model.UserFavorite

class ViewModelDBHelper(
        listingsList: MutableLiveData<List<Listing>>,
) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        dbFetchListings(listingsList)
    }
    private fun elipsizeString(string: String) : String {
        if(string.length < 10)
            return string
        return string.substring(0..9) + "..."
    }

    /////////////////////////////////////////////////////////////
    // Interact with Firestore db
    // https://firebase.google.com/docs/firestore/query-data/get-data
    //
    // If we want to listen for real time updates use this
    // .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    // But be careful about how listener updates live data
    // and noteListener?.remove() in onCleared
    fun dbFetchListings(listingsList: MutableLiveData<List<Listing>>) {
        db.collection("allListings")
            .orderBy("timeStamp")//, Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "allListings fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                listingsList.postValue(result.documents.mapNotNull {
                    it.toObject(Listing::class.java)
                })
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "allListings fetch FAILED ", it)
            }
    }

    fun createListing(
        listing: Listing,
        listingsList: MutableLiveData<List<Listing>>
    ) {
        listing.listingID = db.collection("allListings").document().id
        db.collection("allListings")
            .document(listing.listingID)
            .set(listing)
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "Listing create \"${elipsizeString(listing.title.toString())}\" id: ${listing.listingID}"
                )
                dbFetchListings(listingsList)
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Listing create FAILED \"${elipsizeString(listing.title.toString())}\"")
                Log.w(javaClass.simpleName, "Error ", e)
            }
    }
}