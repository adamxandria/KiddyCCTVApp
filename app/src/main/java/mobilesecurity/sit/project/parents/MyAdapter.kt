package mobilesecurity.sit.project.parents

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import mobilesecurity.sit.project.R
import java.util.ArrayList

class MyAdapter(private val dataList: ArrayList<DataClass>, private val context: Context) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.staggered_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataItem = dataList[position]
        // Check if imageURL is not null or blank before using it with Glide
        if (!dataItem.url.isNullOrBlank()) {
            Glide.with(context)
                .load(dataItem.url)
                .into(holder.staggeredImages)
        } else {
            // Here you can set a placeholder image or handle the null case as you prefer
            holder.staggeredImages.setImageResource(R.drawable.visa) // Replace with an actual placeholder image resource
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var staggeredImages: RoundedImageView = itemView.findViewById(R.id.staggeredImages)
    }
}