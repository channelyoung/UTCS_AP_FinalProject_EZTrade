package edu.utap.eztrade.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.eztrade.MainActivity
import edu.utap.eztrade.MainViewModel
import edu.utap.eztrade.R
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: ListingAdapter

    private fun actionSearch() {
        viewModel.observeSearchRadiusListings().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })


        (activity as AppCompatActivity).searchET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty() && activity != null) (activity as MainActivity).hideKeyboard()
                viewModel.setSearchTerm(s.toString())
            }
        })
    }

    private fun initRecyclerView(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.listingRV)
        adapter = ListingAdapter(viewModel, 0) { position, from ->
            val action = SearchFragmentDirections.actionSearchFragmentToOneListingFragment(position, from)
            findNavController().navigate(action)
        }
        rv.adapter = adapter
        rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun initSpinner(view: View) {       // Setup distance spinner
        val distances = resources.getStringArray(R.array.Distance)
        val spinner = view.findViewById<Spinner>(R.id.distanceSpinner)
        if (spinner != null) {
            val distanceAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, distances)
            spinner.adapter = distanceAdapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    when (spinner.selectedItemPosition) {
                        0 -> viewModel.setRadius(-1.0)
                        1 -> viewModel.setRadius(5.0)
                        2 -> viewModel.setRadius(10.0)
                        3 -> viewModel.setRadius(20.0)
                        4 -> viewModel.setRadius(50.0)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_search, container, false)
        viewModel.observeListings()
        initSpinner(view)
        initRecyclerView(view)
        viewModel.observeSearchRadiusListings().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionSearch()

        // Display listings

    }
}