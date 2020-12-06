package edu.utap.eztrade.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import edu.utap.eztrade.MainViewModel
import edu.utap.eztrade.R
import edu.utap.eztrade.model.Listing
import kotlinx.android.synthetic.main.fragment_one_listing.*

class OneListingFragment: Fragment(R.layout.fragment_one_listing) {
    private lateinit var listing: Listing
    private var position = -1
    private val viewModel: MainViewModel by activityViewModels()
    private val args: OneListingFragmentArgs by navArgs()

    private fun initPagerView(root: View) {
        position = args.position

        listing = if(position == -1) {
            Listing()
        } else {
            if (args.fromFragment == 0) {
                viewModel.getSearchListing(position)
            } else {
                viewModel.getFavListing(position)
            }
        }

        val pager = root.findViewById<ViewPager>(R.id.viewPager)
        val adapter = OneListingImageAdapter(listing.pictureUUIDs, this.requireContext(), viewModel)
        pager.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPagerView(view)
        titleTV_one.text = listing.title
        userTV_one.text = listing.name
        detail_one.text = listing.detail
        if (viewModel.isFav(listing)) {
            favIV_one.setImageResource(R.drawable.ic_favorite_black_24dp)
        } else {
            favIV_one.setImageResource(R.drawable.ic_favorite_border_black_24dp)
        }

        favIV_one.setOnClickListener {
            if (viewModel.isFav(listing)) {
                viewModel.removeFav(listing)
                favIV_one.setImageResource(R.drawable.ic_favorite_border_black_24dp)
            } else {
                viewModel.addFav(listing)
                favIV_one.setImageResource(R.drawable.ic_favorite_black_24dp)
            }
        }

        oneListingLayout.setOnClickListener{
            viewModel.setSearchTerm("")
            if (args.fromFragment == 1) {
                findNavController().navigate(OneListingFragmentDirections.actionOneListingFragmentToFavoriteFragment())
            } else {
                findNavController().navigate(OneListingFragmentDirections.actionOneListingFragmentToSearchFragment())
            }
        }
    }
}