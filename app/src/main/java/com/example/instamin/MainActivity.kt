package com.example.instamin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instamin.models.Post
import com.example.instamin.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG ="MainActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
open class MainActivity : AppCompatActivity() {

    private var signedInUser : User? = null
    private lateinit var fabCreate:FloatingActionButton
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var rvPosts: RecyclerView
    private lateinit var adapter: PostsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabCreate= findViewById(R.id.floatingBtn)
        //create a ui for the rv - done
        //create an adapter
        //create a data source- done
        posts= mutableListOf()
        //bind the adapter and layout manager to the rv
        adapter= PostsAdapter(this,posts)
        rvPosts= findViewById(R.id.rvPosts)
        rvPosts.adapter= adapter
        rvPosts.layoutManager= LinearLayoutManager(this)

        firestoreDb= FirebaseFirestore.getInstance()


        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener {
                signedInUser = it.toObject(User::class.java)
                Log.i(TAG,"signed in user: $signedInUser")
            }

            .addOnFailureListener { exception ->
                Log.i(TAG,"failure fetching signed in user",exception)
            }

        var postsReference = firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username!=null){
            supportActionBar?.title = username
            postsReference= postsReference.whereEqualTo("user.username",username)
        }

        postsReference.addSnapshotListener { snapshot, exception ->
           if (exception!=null || snapshot == null){
               Log.e(TAG,"exception when querying posts",exception)
               return@addSnapshotListener
           }

            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) {
                Log.i(TAG,"Post ${post}")
            }
        }
        fabCreate.setOnClickListener{
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId== R.id.profile) {
           val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME ,"jethalal")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}