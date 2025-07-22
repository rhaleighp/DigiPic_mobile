package com.example.digipic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.*
import java.io.File
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException


class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var uploadButton: ImageView
    private lateinit var userNameText: TextView

    private val PICK_IMAGE_REQUEST = 101
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        profileImage = findViewById(R.id.profileImage)
        uploadButton = findViewById(R.id.uploadButton)
        userNameText = findViewById(R.id.userNameText)

        val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = prefs.getString("userUsername", "unknown")

        userNameText.text = username

        uploadButton.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                uploadProfilePhoto(imageUri)
            }
        }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val filePath = getRealPathFromURI(uri)
        if (filePath == null) {
            Toast.makeText(this, "Invalid image file", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(filePath)
        val mediaType = "image/*".toMediaTypeOrNull()
        val fileBody = file.asRequestBody(mediaType)

        val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = prefs.getString("userUsername", "unknown")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("username", username ?: "unknown")
            .addFormDataPart("profilePhoto", file.name, fileBody)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:5000/upload-profile-photo")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val photoUrl = "http://10.0.2.2:5000/uploads/profile/${file.name}"
                    runOnUiThread {
                        Glide.with(this@ProfileActivity)
                            .load(photoUrl)
                            .into(profileImage)
                        Toast.makeText(this@ProfileActivity, "Profile photo updated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return null
    }
}
