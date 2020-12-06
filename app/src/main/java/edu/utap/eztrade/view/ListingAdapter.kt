package edu.utap.eztrade.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.eztrade.MainViewModel
import edu.utap.eztrade.R
import edu.utap.eztrade.model.Listing


class ListingAdapter(private val viewModel: MainViewModel,
                     private val fromFragment: Int,
                     private val oneListing: (Int, Int)->Unit)
    : ListAdapter<Listing, ListingAdapter.VH>(Diff()) {
    // This class allows the adapter to compute what has changed
    class Diff : DiffUtil.ItemCallback<Listing>() {
        override fun areItemsTheSame(oldItem: Listing, newItem: Listing): Boolean {
            return oldItem.listingID == newItem.listingID
        }

        override fun areContentsTheSame(oldItem: Listing, newItem: Listing): Boolean {
            return oldItem.listingID == newItem.listingID
                    && oldItem.title == newItem.title
                    && oldItem.ownerUid == newItem.ownerUid
                    && oldItem.pictureUUIDs == newItem.pictureUUIDs
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.detail == newItem.detail
        }
    }

    // Puts the time first, which is most important.  But date is useful too
    private val dateFormat: DateFormat =
        SimpleDateFormat("hh:mm:ss MM-dd-yyyy")

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private var title: TextView = view.findViewById(R.id.titleTV)
        private var coverPhoto: ImageView = view.findViewById(R.id.coverPhotoIV)
        private var noPhoto: TextView = view.findViewById(R.id.noPhotoTV)
        private var fav: ImageView = view.findViewById(R.id.favIV)

        init {
            fav.setOnClickListener {
                val position = adapterPosition
                if (viewModel.isFav(getItem(position))) {
                    viewModel.removeFav(getItem(position))
                } else {
                    viewModel.addFav(getItem(position))
                }
                notifyItemChanged(position)
            }

            coverPhoto.setOnClickListener {
                oneListing(adapterPosition, fromFragment)
            }
        }

        private fun bindCoverPhoto(imageList: List<String>) {
            if(imageList.isNotEmpty()) {
                noPhoto.visibility = View.GONE
                viewModel.glideFetch(imageList[0], coverPhoto)
            } else {
                noPhoto.visibility = View.VISIBLE
                coverPhoto.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }

        fun bind(listing: Listing) {
            title.text = listing.title
            bindCoverPhoto(listing.pictureUUIDs)
            if (viewModel.isFav(listing)) {
                fav.setImageResource(R.drawable.ic_favorite_black_24dp)
            } else {
                fav.setImageResource(R.drawable.ic_favorite_border_black_24dp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.listing_row, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(holder.adapterPosition))
    }
}