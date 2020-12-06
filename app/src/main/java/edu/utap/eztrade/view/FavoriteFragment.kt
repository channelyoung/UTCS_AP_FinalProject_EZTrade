package edu.utap.eztrade.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.eztrade.MainViewModel
import edu.utap.eztrade.R
import kotlinx.android.synthetic.main.fragment_favorite.*

class FavoriteFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: ListingAdapter

    private fun initRecyclerView(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.listingRV_favorite)
        adapter = ListingAdapter(viewModel, 1) { position, from ->
            val action = FavoriteFragmentDirections.actionFavoriteFragmentToOneListingFragment(position, from)
            findNavController().navigate(action)
        }
        rv.adapter = adapter
        rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun initFavoritesObservers() {
        viewModel.observeFav().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        initRecyclerView(view)
        initFavoritesObservers()
        return view
    }
}