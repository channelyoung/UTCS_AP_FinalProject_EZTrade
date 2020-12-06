package edu.utap.eztrade.view

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.eztrade.MainViewModel
import edu.utap.eztrade.R
import edu.utap.eztrade.view.PostAddFragmentDirections.Companion.actionPostAddFragmentToSearchFragment
import kotlinx.android.synthetic.main.fragment_postadd.*


class PostAddFragment : Fragment() {
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var pictureUUIDs: List<String>
    private val viewModel: MainViewModel by activityViewModels()
    private var lat: Float = 0.0f
    private var lon: Float = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_postadd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pictureUUIDs = listOf()
        photosRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imageAdapter = ImageAdapter(viewModel) { pictureUUIDPosition ->
            Log.d(javaClass.simpleName, "pictureUUIDs del $pictureUUIDPosition")
            val shorterList = pictureUUIDs.toMutableList()
            shorterList.removeAt(pictureUUIDPosition)
            pictureUUIDs = shorterList
            imageAdapter.submitList(pictureUUIDs)
        }
        photosRV.adapter = imageAdapter
        imageAdapter.submitList(pictureUUIDs)

        viewModel.observeLocation().observe(viewLifecycleOwner, Observer{
            lat = it.latitude.toFloat()
            lon = it.longitude.toFloat()
        })

        saveButton?.setOnClickListener {
            if (TextUtils.isEmpty(titleET.text.toString())) {
                Toast.makeText(
                    activity,
                    "Enter title!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Log.d(javaClass.simpleName, "create note $titleET len ${pictureUUIDs.size}")
                viewModel.createListing(
                    titleET.text.toString(),
                    detailET.text.toString(),
                    lat,
                    lon,
                    pictureUUIDs
                )
                findNavController().navigate(actionPostAddFragmentToSearchFragment())
            }
        }

        cancelButton?.setOnClickListener {
            findNavController().navigate(actionPostAddFragmentToSearchFragment())
        }

        takePhotoIB?.setOnClickListener{
            viewModel.takePhoto(::pictureSuccess)
        }

        photoLibraryIB?.setOnClickListener{
            viewModel.selectPhoto(::pictureSuccess)
        }
    }

    private fun pictureSuccess(pictureUUID: String) {
        // Add new image path to imageList
        pictureUUIDs.toMutableList().apply{
            add(pictureUUID)
            Log.d(javaClass.simpleName, "photo added $pictureUUID len ${this.size}")
            pictureUUIDs = this
            imageAdapter.submitList(pictureUUIDs)
        }
    }
}