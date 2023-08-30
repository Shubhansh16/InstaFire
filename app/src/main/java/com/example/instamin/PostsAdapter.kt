package com.example.instamin


import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instamin.models.Post

class PostsAdapter ( val context: Context,  val posts: List<Post>): RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.item_posts,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount()= posts.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(posts[position])
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {

        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvRelativeTime :TextView= itemView.findViewById(R.id.tvRelativeTime)
        val ivPost :ImageView= itemView.findViewById(R.id.ivPost)
        val tvDescription:TextView= itemView.findViewById(R.id.tvDescription)

        fun bind(post: Post) {
               tvUsername.text= post.user?.username
               tvDescription.text= post.description
               Glide.with(context).load(post.image_url).into(ivPost)
               tvRelativeTime.text= DateUtils.getRelativeTimeSpanString(post.creation_time_ms)
        }
    }
}
