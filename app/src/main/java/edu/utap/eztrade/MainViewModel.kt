package edu.utap.eztrade

import android.app.Application
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import edu.utap.eztrade.glide.Glide
import edu.utap.eztrade.model.Listing
import edu.utap.eztrade.model.UserFavorite
import java.io.File
import java.io.IOException
import java.util.*

class MainViewModel(application: Application) :
    AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext
    // Remember the uuid, and hence file name of file camera will create
    private var pictureUUID: String =""
    private var storageDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    // LiveData for entire listings, favorite listings
    private var listingsList = MutableLiveData<List<Listing>>()
    private var favoritelist = MutableLiveData<List<Listing>>(mutableListOf())
    private var searchTerm = MutableLiveData("")
    private var searchListings= MediatorLiveData<List<Listing>>().apply {
        value = listingsList.value
    }
    private var searchRadiusListings= MediatorLiveData<List<Listing>>().apply {
        value = listingsList.value
    }
    private var location = MutableLiveData<Location>().apply {
        value = Location("")
    }
    private var radius = MutableLiveData(0.0)
    // Firestore state
    private lateinit var auth: Auth
    private lateinit var storage: Storage
    // Database access
    private val dbHelp = ViewModelDBHelper(listingsList)
    // assert does not work
    private lateinit var crashMe: String
    private val mile2meter = 1609.34

    // NB: Here is a problem with this whole strategy.  It "works" when you use
    // local variables to save these "function pointers."  But the viewModel can be
    // cleared, so we want to save these function pointers that are actually closures
    // with a reference to the activity/fragment that created them.  So we get a
    // parcelable error if we try to store them into a SavedHandleState
    private var takePhotoIntent: () -> Unit = ::noPhoto
    private var selectPhotoIntent: () -> Unit = ::noPhoto

    private fun noPhoto() {
        Log.d(
            javaClass.simpleName,
            "Function must be initialized to something that can start the camera intent"
        )
        crashMe.plus(" ")
    }

    private fun defaultPhoto(@Suppress("UNUSED_PARAMETER") path: String) {
        Log.d(javaClass.simpleName, "Function must be initialized to photo callback")
        crashMe.plus(" ")
    }
    private var photoSuccess: (path: String) -> Unit = ::defaultPhoto

    /////////////////////////////////////////////////////////////
    fun observeListings() {
        dbHelp.dbFetchListings(listingsList)
    }

    fun isListingsEmpty(): Boolean {
        return listingsList.value.isNullOrEmpty()
    }

    fun createListing(title: String, detail: String, lat: Float, lon: Float, pictureUUIDs: List<String>) {
        val listing = Listing(
            name = auth.getDisplayName(),
            ownerUid = auth.getUid(),
            title = title,
            detail = detail,
            latitude = lat,
            longitude = lon,
            pictureUUIDs = pictureUUIDs
            // dbHelp sets noteID
        )
        dbHelp.createListing(listing, listingsList)
    }

    //////////////////////////////////////////////////////////////////
    // Favorites
    fun observeFav(): LiveData<List<Listing>> {
        return favoritelist
    }

    fun isFav(listing: Listing): Boolean{
        return favoritelist.value?.contains(listing) ?: false
    }

    fun addFav(listing: Listing) {
        val locallist = favoritelist.value?.toMutableList()
        locallist?.let{
            it.add(listing)
            favoritelist.value = it
        }
    }

    fun removeFav(listing: Listing) {
        val locallist = favoritelist.value?.toMutableList()
        locallist?.let {
            it.remove(listing)
            favoritelist.value = it
        }
    }

    fun getFavListing(position: Int) : Listing {
        val listing = favoritelist.value?.get(position)
        Log.d(javaClass.simpleName, "notesList.value ${listingsList.value}")
        Log.d(javaClass.simpleName, "getNode $position list len ${listingsList.value?.size}")
        return listing!!
    }

    //////////////////////////////////////////////////////////////////
    // Search
    fun getSearchListing(position: Int) : Listing {
        val listing = searchListings.value?.get(position)
        return listing!!
    }

    private fun setSpan(fulltext: String, subtext: String): Boolean {
        if (subtext.isEmpty()) return true
        val i = fulltext.indexOf(subtext, ignoreCase = true)
        if (i == -1) return false
//        fulltext.setSpan(
//            ForegroundColorSpan(Color.BLUE), i, i + subtext.length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
        return true
    }

    // Given a search string, look for it in the listing.  If found,
    // highlight it and return true, otherwise return false.
    private fun searchFor(listing: Listing, searchTerm: String): Boolean {
//        listing.title.clearSpans()
//        listing.detail.clearSpans()
        val titleFound: Boolean = setSpan(listing.title, searchTerm)
        val detailFound: Boolean = setSpan(listing.detail, searchTerm)
        return titleFound || detailFound
    }

    fun observeSearchListings(): LiveData<List<Listing>> {
        return searchListings
    }

    fun setSearchTerm(searchKey: String) {
        searchTerm.value = searchKey
    }

    private fun filterListings(): List<Listing> {
        return if (listingsList.value ==  null) {
            listOf()
        } else {
            val searchTermValue = searchTerm.value!!
            listingsList.value!!.filter {
                searchFor(it, searchTermValue)
            }
        }
    }

    //////////////////////////////////////////////////////////////////
    // Location
    fun setLocation(lat: Double, lon: Double) {
        location.value?.latitude = lat
        location.value?.longitude = lon
    }

    fun observeLocation(): LiveData<Location> {
        return location
    }

    fun setRadius(rad: Double) {
        radius.value = rad
    }

    private fun calculateDistance(listing: Listing): Boolean {
        val loc = Location("")
        loc.latitude = listing.latitude.toDouble()
        loc.longitude = listing.longitude.toDouble()
        return if (radius.value == -1.0) {
            true
        } else {
            Log.d("Location", "${location.value?.distanceTo(loc)!!}")
            location.value?.distanceTo(loc)!! <= radius.value?.times(mile2meter) ?: 0.0
        }
    }

    private fun filterListingsByRadius(): List<Listing> {
        return if (searchListings.value == null) {
            listOf()
        } else {
            searchListings.value!!.filter {
                calculateDistance(it)
            }
        }
    }

    fun observeSearchRadiusListings(): LiveData<List<Listing>> {
        return searchRadiusListings
    }

    init {
        searchListings.addSource(listingsList) {
            searchListings.value = filterListings()}

        searchListings.addSource(searchTerm) {
            searchListings.value = filterListings()}

        searchRadiusListings.addSource(searchListings) {
            searchRadiusListings.value = filterListingsByRadius()}

        searchRadiusListings.addSource(radius) {
            searchRadiusListings.value = filterListingsByRadius()}
    }

    /////////////////////////////////////////////////////////////
    // This is intended to be set once by MainActivity.
    // The bummer is that taking a photo requires startActivityForResult
    // which has to be called from an activity.
    fun setTakePhotoIntent(_takePhotoIntent: () -> Unit) {
        takePhotoIntent = _takePhotoIntent
    }

    fun setSelectPhotoIntent(_selectPhotoIntent: () -> Unit) {
        selectPhotoIntent = _selectPhotoIntent
    }

    /////////////////////////////////////////////////////////////
    // Get callback for when camera intent returns.
    // Send intent to take picture
    fun takePhoto(_photoSuccess: (String) -> Unit) {
        photoSuccess = _photoSuccess
        takePhotoIntent()
    }

    fun selectPhoto(_photoSuccess: (String) -> Unit) {
        photoSuccess = _photoSuccess
        selectPhotoIntent()
    }

    /////////////////////////////////////////////////////////////
    // Create a file for the photo, remember it, and create a Uri
    fun getPhotoURI(): Uri {
        // Create an image file name
        pictureUUID = UUID.randomUUID().toString()
        var photoUri: Uri? = null
        // Create the File where the photo should go
        try {
            val localPhotoFile = File(storageDir, "${pictureUUID}.jpg")
            Log.d(javaClass.simpleName, "photo path ${localPhotoFile.absolutePath}")
            photoUri = FileProvider.getUriForFile(
                appContext,
                "edu.utap.eztrade",
                localPhotoFile
            )
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.d(javaClass.simpleName, "Cannot create file", ex)
        }
        // CRASH.  Production code should do something more graceful
        return photoUri!!
    }

    fun setSelectPhotoUUID() {
        pictureUUID = UUID.randomUUID().toString()
    }

    fun selectPictureSuccess(uri: Uri) {
        storage.uploadSelectImage(uri, pictureUUID) {
            photoSuccess(pictureUUID)
            photoSuccess = ::defaultPhoto
            pictureUUID = ""
        }
    }

    /////////////////////////////////////////////////////////////
    // Callbacks from MainActivity.getResultForActivity from camera intent
    // We can't just schedule the file upload and return.
    // The problem is that our previous picture uploads can still be pending.
    // So a note can have a pictureUUID that does not refer to an existing file.
    // That violates referential integrity, which we really like in our db (and programming
    // model).
    // So we do not add the pictureUUID to the note until the picture finishes uploading.
    // That means a user won't see their picture updates immediately, they have to
    // wait for some interaction with the server.
    // You could imagine dealing with this somehow using local files while waiting for
    // a server interaction, but that seems error prone.
    // Freezing the app during an upload also seems bad.
    fun pictureSuccess() {
        val localPhotoFile = File(storageDir, "${pictureUUID}.jpg")
        Log.d("YYYY", "$localPhotoFile")
        // Wait until photo is successfully uploaded before calling back
        storage.uploadImage(localPhotoFile, pictureUUID) {
            photoSuccess(pictureUUID)
            photoSuccess = ::defaultPhoto
            pictureUUID = ""
        }
    }
    fun pictureFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
    }

    fun firestoreInit(auth: Auth, storage: Storage) {
        this.auth = auth
        this.storage = storage
    }

    fun glideFetch(pictureUUID: String, imageView: ImageView) {
        Glide.fetch(
            storage.uuid2StorageReference(pictureUUID),
            imageView
        )
    }

    fun signout() {
        FirebaseAuth.getInstance().signOut()
    }
}
