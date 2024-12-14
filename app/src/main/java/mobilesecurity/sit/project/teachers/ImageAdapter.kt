package mobilesecurity.sit.project.teachers

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import mobilesecurity.sit.project.R

class ImageAdapter(private var imageUrls: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kidsactivity_images, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(imageUrls[position])
            .centerCrop()
            .placeholder(R.drawable.image_placeholder) // Replace with your placeholder drawable
            .error(R.drawable.image_load_error) // Replace with your error drawable
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    Log.e("ImageAdapter", "Failed to load image", e)
                    return false // Return false to let Glide handle the error image
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false // Return false to let Glide handle the normal image display
                }
            })
            .into(holder.imageView)
    }


    override fun getItemCount(): Int {
        Log.d("ImageAdapter", "Item count: ${imageUrls.size}")
        return imageUrls.size
    }



    fun updateData(newImageUrls: List<String>) {
        Log.d("ImageAdapter", "Updating data with URLs: $newImageUrls")
        imageUrls = newImageUrls
        notifyDataSetChanged()
    }
}

//package mobilesecurity.sit.project.teachers
//
//import android.graphics.drawable.Drawable
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.DataSource
//import com.bumptech.glide.load.engine.GlideException
//import com.bumptech.glide.request.RequestListener
//import com.bumptech.glide.request.target.Target
//import mobilesecurity.sit.project.R
//
//class ImageAdapter(private var imageUrls: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val imageView: ImageView = view.findViewById(R.id.imageView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kidsactivity_images, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Glide.with(holder.imageView.context)
//            .load(imageUrls[position])
//            .placeholder(R.drawable.image_placeholder) // Replace with your placeholder drawable
//            .error(R.drawable.image_load_error) // Replace with your error drawable
//            .listener(object : RequestListener<Drawable> {
//                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                    Log.e("ImageAdapter", "Failed to load image", e)
//                    return false // Return false to let Glide handle the error image
//                }
//
//                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                    return false // Return false to let Glide handle the normal image display
//                }
//            })
//            .into(holder.imageView)
//    }
//
//
//    override fun getItemCount(): Int {
//        Log.d("ImageAdapter", "Item count: ${imageUrls.size}")
//        return imageUrls.size
//    }
//
//
//
//    fun updateData(newImageUrls: List<String>) {
//        Log.d("ImageAdapter", "Updating data with URLs: $newImageUrls")
//        imageUrls = newImageUrls
//        notifyDataSetChanged()
//    }
//}