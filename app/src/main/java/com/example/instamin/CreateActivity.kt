package com.example.instamin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.instamin.models.Post
import com.example.instamin.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG ="CreateActivity"
private const val PICK_PHOTO_CODE = 1234
class CreateActivity : AppCompatActivity() {

    private lateinit var submitBtn:Button
    private var signedInUser:User?=null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var btnPickImage:Button
    private lateinit var imageview:ImageView
    private lateinit var etDescription:EditText
    private var photoUri:Uri?=null
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        btnPickImage=findViewById(R.id.chooseBtn)
        imageview=findViewById(R.id.imageView)
        etDescription=findViewById(R.id.etDescription)
        submitBtn=findViewById(R.id.btnSubmit)

        storageReference=FirebaseStorage.getInstance().reference
        firestoreDb= FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG,"signed in user: $signedInUser")
            }

            .addOnFailureListener { exception ->
                Log.i(TAG,"failure fetching signed in user",exception)
            }

        submitBtn.setOnClickListener {
            handleSubmitButtonClick()
        }

        btnPickImage.setOnClickListener {
            Log.i(TAG,"open up image picker in device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type ="image/*"
            if (imagePickerIntent.resolveActivity(packageManager) !=null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }
    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null){
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (etDescription.text.isBlank()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (signedInUser ==null){
            Toast.makeText(this, "No signed in user", Toast.LENGTH_SHORT).show()
            return
        }
        submitBtn.isEnabled= false

        val photoUploadUri =photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")

        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
             Log.i(TAG,"uploaded bytes ${photoUploadTask.result?.bytesTransferred}")
                 photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoreDb.collection("posts").add(post)

            }.addOnCompleteListener { postCreationTask ->
                submitBtn.isEnabled = true
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG,"Exception during firebase operation", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageview.setImageResource(0)
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME,signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_CODE){
            if (resultCode== Activity.RESULT_OK){
               photoUri= data?.data
                imageview.setImageURI(photoUri)
                Log.i(TAG,"photoUri $photoUri")
            } else {
                Toast.makeText(this, "Image pick action canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}