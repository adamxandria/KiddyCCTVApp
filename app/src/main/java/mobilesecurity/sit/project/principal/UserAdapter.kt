package mobilesecurity.sit.project.principal

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import mobilesecurity.sit.project.R


class UserAdapter(
    private val users: MutableList<User>,
    private val onItemClick: ((String) -> Unit)?
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

//    interface OnUserClickListener {
//        fun onUserClick(userId: String)
//    }



    //var onItemClick: ((String) -> Unit)? = null // Using String for user ID

    // ViewHolder class for holding each user item
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tvAdminName)
        private val emailTextView: TextView = itemView.findViewById(R.id.tvAdminEmail)
        private val adminIconImageView: ImageView = itemView.findViewById(R.id.ivAdminIcon)

        init {
            itemView.setOnClickListener {
                // Make sure users[adapterPosition].id is not null
                users.getOrNull(adapterPosition)?.id?.let { userId ->
                    onItemClick?.invoke(userId)
                }
            }
        }

        fun bind(user: User) {
            nameTextView.text = user.name
            emailTextView.text = user.email

            if (!user.profileImg.isNullOrEmpty()) {
                Picasso.get().load(user.profileImg).placeholder(R.drawable.userprofile).error(R.drawable.error).into(adminIconImageView)
            } else {
                adminIconImageView.setImageResource(R.drawable.profile)
            }
//            itemView.setOnClickListener {
//                listener.onUserClick(user.id)
//            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}