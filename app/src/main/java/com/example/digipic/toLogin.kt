package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class toLogin : AppCompatActivity() {

    private val client    = OkHttpClient()
    private val serverUrl = "http://127.0.0.1:5000/login"

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        // 1) Hook touch on the right drawable of the password field:
        etPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // index: 0=left,1=top,2=right,3=bottom
                val drawables = etPassword.compoundDrawables
                val rightDrawable = drawables[drawableEnd]
                if (rightDrawable != null) {
                    // check if touch is within the drawable bounds
                    val touchX = event.x.toInt()
                    val width   = etPassword.width
                    val padding = etPassword.paddingEnd
                    val drawableWidth = rightDrawable.intrinsicWidth
                    if (touchX >= width - padding - drawableWidth) {
                        togglePasswordVisibility()
                        // consume the event
                        return@setOnTouchListener true
                    }
                }
            }
            // let other touches pass through (cursor positioning, etc.)
            return@setOnTouchListener false
        }

        findViewById<Button>(R.id.getStartedButton).setOnClickListener {
            attemptLogin()
        }

        findViewById<TextView>(R.id.tvSignup).setOnClickListener {
            startActivity(Intent(this, toSignup::class.java))
        }

        findViewById<TextView>(R.id.tvForgot).setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // hide password
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etPassword.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                getDrawable(R.drawable.ic_visibility_off),
                null
            )
        } else {
            // show password
            etPassword.transformationMethod = null
            etPassword.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                getDrawable(R.drawable.ic_visibility),
                null
            )
        }
        isPasswordVisible = !isPasswordVisible
        // keep cursor at end
        etPassword.setSelection(etPassword.text.length)
    }

    private fun attemptLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(serverUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                runOnUiThread {
                    Toast.makeText(
                        this@toLogin,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string()
                if (response.isSuccessful && respBody != null) {
                    handleSuccessfulResponse(respBody)
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@toLogin,
                            "Login failed: ${respBody ?: response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun handleSuccessfulResponse(respBody: String) {
        try {
            val root     = JSONObject(respBody)
            val userObj  = root.getJSONObject("user")
            val email    = userObj.getString("email")
            val uname    = userObj.getString("username")

            val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("isLoggedIn",   true)
                putString("userUsername",  uname)
                putString("userEmail",     email)
                apply()
            }

            runOnUiThread {
                Toast.makeText(this@toLogin, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@toLogin, HomeActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this@toLogin,
                    "Parse error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
