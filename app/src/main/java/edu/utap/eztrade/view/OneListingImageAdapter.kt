package edu.utap.eztrade.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.utap.eztrade.MainViewModel
import edu.utap.eztrade.R
import androidx.viewpager.widget.PagerAdapter
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import edu.utap.eztrade.glide.Glide

class OneListingImageAdapter(private val images: List<String>, private val context: Context, private val viewModel: MainViewModel) :
    PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as LinearLayout
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val pictureUUID = images[position]
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.one_listing_image, container, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView_one)
        viewModel.glideFetch(pictureUUID, imageView)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }


}