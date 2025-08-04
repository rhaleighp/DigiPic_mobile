package com.example.digipic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 101
        // TODO: replace with your PC’s LAN IP
        private const val SERVER_IP = "127.0.0.1"
    }

    private lateinit var profileImage: ImageView
    private lateinit var uploadButton: ImageView
    private lateinit var userNameText: TextView

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        profileImage  = findViewById(R.id.profileImage)
        uploadButton  = findViewById(R.id.uploadButton)
        userNameText  = findViewById(R.id.userNameText)

        val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)

        // 1) Load and display username
        userNameText.text = prefs.getString("userUsername", "Unknown")

        // 2) Load & display persisted photo URL (if any)
        prefs.getString("userPhotoUrl", null)?.let { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .into(profileImage)
        }

        uploadButton.setOnClickListener {
            openImagePicker()
        }

        setupBottomNavigation()
    }

    private fun openImagePicker() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST
            && resultCode == Activity.RESULT_OK
            && data?.data != null) {
            uploadProfilePhoto(data.data!!)
        }
    }

    private fun uploadProfilePhoto(imageUri: Uri) {
        // Resolve local file path
        val filePath = getRealPathFromURI(imageUri)
        if (filePath.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid image file", Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(filePath)

        // Load userEmail
        val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", "") ?: ""
        if (userEmail.isBlank()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Build multipart
        val fileBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("profilePic", file.name, fileBody)
            .build()

        // Call mobile-profile-photo
        val url = "http://$SERVER_IP:5000/users/${Uri.encode(userEmail)}/mobile-profile-photo"
        val request = Request.Builder().url(url).post(multipart).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && bodyString != null) {
                        try {
                            val json     = JSONObject(bodyString)
                            val photoPath= json.getString("photoURL")
                            val fullUrl  = "http://$SERVER_IP:5000$photoPath"

                            // 1) Display
                            Glide.with(this@ProfileActivity)
                                .load(fullUrl)
                                .into(profileImage)

                            // 2) Persist for next time
                            prefs.edit()
                                .putString("userPhotoUrl", fullUrl)
                                .apply()

                            Toast.makeText(
                                this@ProfileActivity,
                                "Profile photo updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Parse error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Upload failed: ${bodyString ?: response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, proj, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(idx)
            }
        }
        return null
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageView>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageView>(R.id.mainGallery).setOnClickListener {
            startActivity(Intent(this, NewsFeedActivity::class.java))
        }
        findViewById<ImageView>(R.id.navLessons).setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
        }
    }
}
