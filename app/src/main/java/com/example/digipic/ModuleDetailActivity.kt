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
import okhttp3.Callback
import okhttp3.Response
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
    private lateinit var subLessonsHeader: TextView
    private lateinit var subLessonsContainer: LinearLayout
    private lateinit var markCompletedButton: Button
    private lateinit var quizButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var attachmentsContainer: LinearLayout

    private val client = OkHttpClient()
    private var fetchedQuizId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_info_txt)

        // Bind views
        headerText           = findViewById(R.id.title)
        moduleTitle          = findViewById(R.id.textTitle)
        moduleContent        = findViewById(R.id.textContent)
        subLessonsHeader     = findViewById(R.id.subLessonsHeader)
        subLessonsContainer  = findViewById(R.id.subLessonsContainer)
        markCompletedButton  = findViewById(R.id.btnMarkCompleted)
        quizButton           = findViewById(R.id.quizBtn)
        progressBar          = findViewById(R.id.progressBar)
        attachmentsContainer = findViewById(R.id.attachmentsContainer)

        setupBottomNavigation()

        val moduleId = intent.getStringExtra("moduleId")
        val courseId = intent.getStringExtra("courseId")
        if (moduleId.isNullOrBlank() || courseId.isNullOrBlank()) {
            Toast.makeText(this, "Missing module or course ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Hide quiz until available
        quizButton.visibility = View.GONE

        // Show loading spinner
        progressBar.visibility = View.VISIBLE

        // Fetch module data
        val moduleUrl = "http://$SERVER_IP:5000/modules/$moduleId"
        client.newCall(Request.Builder().url(moduleUrl).get().build()).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ModuleDetailActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (!response.isSuccessful || body == null) {
                        Toast.makeText(this@ModuleDetailActivity, "Server error: ${response.message}", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    try {
                        val moduleJson = JSONObject(body)
                        moduleJson.put("id", moduleId)
                        bindModuleData(moduleJson, courseId)
                        fetchQuizForModule(moduleId)
                    } catch (ex: Exception) {
                        Toast.makeText(this@ModuleDetailActivity, "Parse error: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun fetchQuizForModule(moduleId: String) {
        val quizUrl = "http://$SERVER_IP:5000/quizzes"
        client.newCall(Request.Builder().url(quizUrl).get().build()).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                try {
                    val quizzes = JSONObject(body).optJSONArray("quizzes") ?: JSONArray()
                    for (i in 0 until quizzes.length()) {
                        val q = quizzes.getJSONObject(i)
                        if (q.optString("moduleId") == moduleId) {
                            fetchedQuizId = q.optString("id")
                            break
                        }
                    }
                } catch (_: Exception) {}
                runOnUiThread {
                    fetchedQuizId?.let {
                        quizButton.visibility = View.VISIBLE
                        quizButton.setOnClickListener { _ ->
                            startActivity(Intent(this@ModuleDetailActivity, QuizActivity::class.java).apply {
                                putExtra("quizId", it)
                                putExtra("moduleId", moduleId)
                            })
                        }
                    }
                }
            }
        })
    }

    private fun bindModuleData(module: JSONObject, courseId: String) {
        headerText.text    = module.optString("type", "TEXT").uppercase()
        moduleTitle.text   = module.optString("title", "Untitled")
        moduleContent.text = module.optString("description", "")

        // Check completion and setup button
        val userEmail = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE).getString("userEmail", "") ?: ""
        if (userEmail.isNotBlank()) {
            checkIfCompleted(userEmail, module.getString("id")) { completed ->
                if (completed) {
                    markCompletedButton.apply {
                        isEnabled = false
                        text = "Completed"
                    }
                } else {
                    markCompletedButton.apply {
                        isEnabled = true
                        setOnClickListener { _ -> markModuleComplete(userEmail, module.getString("id"), courseId) }
                    }
                }
            }
        }

        // Display sub-lessons
        module.optJSONArray("moduleSubTitles")?.takeIf { it.length() > 0 }?.let {
            displaySubLessons(it, module.optJSONArray("moduleSubDescs"))
        }

        // Display attachments
        module.optJSONArray("attachments")?.let { displayAttachments(it) }
    }

    private fun markModuleComplete(email: String, moduleId: String, courseId: String) {
        val payload = JSONObject().apply {
            put("email", email)
            put("moduleId", moduleId)
            put("courseId", courseId)
        }
        client.newCall(Request.Builder()
            .url("http://$SERVER_IP:5000/mark-module-complete")
            .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build())
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) = runOnUiThread {
                    Toast.makeText(this@ModuleDetailActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(call: Call, response: Response) = runOnUiThread {
                    if (response.isSuccessful) {
                        markCompletedButton.apply {
                            isEnabled = false
                            text = "Completed"
                        }
                        Toast.makeText(this@ModuleDetailActivity, "Module and course completion recorded!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ModuleDetailActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun displaySubLessons(titles: JSONArray, descs: JSONArray?) {
        subLessonsContainer.removeAllViews()
        subLessonsHeader.visibility    = View.VISIBLE
        subLessonsContainer.visibility = View.VISIBLE

        for (i in 0 until titles.length()) {
            val tvTitle = TextView(this).apply {
                text = titles.optString(i)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(8), 0, dpToPx(4))
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            subLessonsContainer.addView(tvTitle)
            descs?.optString(i)?.takeIf { it.isNotBlank() }?.let { text ->
                val tvDesc = TextView(this).apply {
                    this.text = text
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setPadding(0, 0, 0, dpToPx(8))
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                subLessonsContainer.addView(tvDesc)
            }
        }
    }

    private fun displayAttachments(arr: JSONArray) {
        attachmentsContainer.removeAllViews()
        for (i in 0 until arr.length()) {
            val att  = arr.getJSONObject(i)
            val path = att.optString("filePath", null)
            val desc = att.optString("description", "")
            path?.let {
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.attachment_height)).apply {
                        bottomMargin = resources.getDimensionPixelSize(R.dimen.attachment_margin)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                Glide.with(this).load("http://$SERVER_IP:5000$it").into(iv)
                attachmentsContainer.addView(iv)
            }
            desc.takeIf { it.isNotBlank() }?.let {
                val tv = TextView(this).apply {
                    text = it
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = resources.getDimensionPixelSize(R.dimen.attachment_margin)
                    }
                }
                attachmentsContainer.addView(tv)
            }
        }
        if (attachmentsContainer.childCount > 0) attachmentsContainer.visibility = View.VISIBLE
    }

    private fun checkIfCompleted(email: String, moduleId: String, callback: (Boolean) -> Unit) {
        val url = "http://$SERVER_IP:5000/users/${Uri.encode(email)}/completed-modules-count"
        client.newCall(Request.Builder().url(url).build()).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) = runOnUiThread { callback(false) }
            override fun onResponse(call: Call, resp: Response) {
                val count = resp.body?.string()?.let { JSONObject(it).optInt("count") } ?: 0
                runOnUiThread { callback(count > 0) }
            }
        })
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.navHome).setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }
        findViewById<ImageView>(R.id.mainGallery).setOnClickListener { startActivity(Intent(this, NewsFeedActivity::class.java)) }
        findViewById<ImageView>(R.id.navLessons).setOnClickListener { startActivity(Intent(this, CourseActivity::class.java)) }
        findViewById<ImageView>(R.id.navProfile).setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        findViewById<ImageView>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
}
