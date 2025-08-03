package com.example.digipic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ModuleDetailActivity : AppCompatActivity() {

    companion object {
        private const val SERVER_IP = "192.168.18.11"
    }

    private lateinit var headerText: TextView
    private lateinit var moduleTitle: TextView
    private lateinit var moduleContent: TextView
    private lateinit var markCompletedButton: Button
    private lateinit var quizButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var attachmentsContainer: LinearLayout

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_info_txt)

        headerText            = findViewById(R.id.title)
        moduleTitle           = findViewById(R.id.textTitle)
        moduleContent         = findViewById(R.id.textContent)
        markCompletedButton   = findViewById(R.id.btnMarkCompleted)
        quizButton            = findViewById(R.id.quizBtn)
        progressBar           = findViewById(R.id.progressBar)
        attachmentsContainer  = findViewById(R.id.attachmentsContainer)

        setupBottomNavigation()

        val moduleId = intent.getStringExtra("moduleId")
        val courseId = intent.getStringExtra("courseId")

        if (moduleId.isNullOrBlank()) {
            Toast.makeText(this, "Missing module ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show loading spinner
        progressBar.visibility = View.VISIBLE

        // Fetch module JSON
        val url = "http://$SERVER_IP:5000/modules/$moduleId"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ModuleDetailActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (!response.isSuccessful || body == null) {
                        Toast.makeText(
                            this@ModuleDetailActivity,
                            "Server error: ${response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@runOnUiThread
                    }
                    try {
                        val obj = JSONObject(body)
                        bindModuleData(obj, courseId)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ModuleDetailActivity,
                            "Parse error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun bindModuleData(module: JSONObject, courseId: String?) {
        // 1) Header & title & description
        val type  = module.optString("type", "text")
        val title = module.optString("title", "Untitled")
        val desc  = module.optString("description", "")

        headerText.text    = type.uppercase()
        moduleTitle.text   = title
        moduleContent.text = desc

        // 2) Quiz button
        val hasQuiz = module.optBoolean("hasQuiz", false)
        quizButton.isEnabled = hasQuiz && courseId != null
        quizButton.alpha = if (quizButton.isEnabled) 1f else 0.5f
        if (quizButton.isEnabled) {
            quizButton.setOnClickListener {
                val quizId = module.optString("quizId")
                startActivity(Intent(this, QuizActivity::class.java).apply {
                    putExtra("quizId", quizId)
                    putExtra("moduleId", module.getString("id"))
                    putExtra("courseId", courseId)
                })
            }
        }

        // 3) Mark completed
        val prefs     = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", "") ?: ""
        if (userEmail.isNotBlank()) {
            checkIfCompleted(userEmail, module.getString("id")) { completed ->
                if (completed) {
                    markCompletedButton.isEnabled = false
                    markCompletedButton.text      = "Completed"
                } else {
                    markCompletedButton.setOnClickListener {
                        markModuleComplete(userEmail, module.getString("id"), courseId)
                    }
                }
            }
        }

        // 4) Attachments
        if (module.has("attachments")) {
            displayAttachments(module.getJSONArray("attachments"))
        }
    }

    private fun displayAttachments(arr: JSONArray) {
        attachmentsContainer.removeAllViews()

        for (i in 0 until arr.length()) {
            val att  = arr.getJSONObject(i)
            val path = att.optString("filePath", null)
            val desc = att.optString("description", "")

            // ImageView
            if (path != null) {
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.attachment_height)
                    ).apply {
                        bottomMargin = resources.getDimensionPixelSize(R.dimen.attachment_margin)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                Glide.with(this)
                    .load("http://$SERVER_IP:5000$path")
                    .into(iv)
                attachmentsContainer.addView(iv)
            }

            // Description
            if (desc.isNotBlank()) {
                val tv = TextView(this).apply {
                    text = desc
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = resources.getDimensionPixelSize(R.dimen.attachment_margin)
                    }
                }
                attachmentsContainer.addView(tv)
            }
        }

        if (attachmentsContainer.childCount > 0) {
            attachmentsContainer.visibility = View.VISIBLE
        }
    }

    private fun checkIfCompleted(
        email: String,
        moduleId: String,
        callback: (Boolean) -> Unit
    ) {
        val url = "http://$SERVER_IP:5000/users/${Uri.encode(email)}/completed-modules-count"
        val req = Request.Builder().url(url).build()
        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { callback(false) }
            }
            override fun onResponse(call: Call, resp: Response) {
                val cnt = resp.body?.string()?.let {
                    JSONObject(it).optInt("count", 0)
                } ?: 0
                runOnUiThread { callback(cnt > 0) }
            }
        })
    }

    private fun markModuleComplete(
        email: String,
        moduleId: String,
        courseId: String?
    ) {
        val json = JSONObject().apply {
            put("email", email)
            put("moduleId", moduleId)
        }
        val body = json.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val req = Request.Builder()
            .url("http://$SERVER_IP:5000/mark-module-complete")
            .post(body)
            .build()

        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ModuleDetailActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            override fun onResponse(call: Call, resp: Response) {
                runOnUiThread {
                    if (resp.isSuccessful) {
                        markCompletedButton.isEnabled = false
                        markCompletedButton.text      = "Completed"
                        Toast.makeText(
                            this@ModuleDetailActivity,
                            "Module marked!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ModuleDetailActivity,
                            "Error: ${resp.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageView>(R.id.mainGallery).setOnClickListener {
            startActivity(Intent(this, NewsFeedActivity::class.java))
        }
        findViewById<ImageView>(R.id.navLessons).setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
        }
        findViewById<ImageView>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
