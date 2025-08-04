package com.example.digipic

import android.content.Intent
import android.os.Bundle
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

    private val client = OkHttpClient()
    // ← Replace with your PC’s actual LAN IP
    private val serverUrl = "http://127.0.0.1:5000/login"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val etUsername  = findViewById<EditText>(R.id.etUsername)
        val etPassword  = findViewById<EditText>(R.id.etPassword)
        val btnLogin    = findViewById<Button>(R.id.getStartedButton)
        val tvSignup    = findViewById<TextView>(R.id.tvSignup)
        val forgetPass  = findViewById<TextView>(R.id.tvForgot)

        tvSignup.setOnClickListener {
            startActivity(Intent(this, toSignup::class.java))
        }
        forgetPass.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build JSON body
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
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@toLogin,
                            "Network error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val respBody = response.body?.string()
                    if (response.isSuccessful && respBody != null) {
                        try {
                            // Parse out user object
                            val root     = JSONObject(respBody)
                            val userObj  = root.getJSONObject("user")
                            val email    = userObj.getString("email")
                            val returnedUsername = userObj.getString("username")

                            // Save into prefs
                            val prefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
                            prefs.edit().apply {
                                putBoolean("isLoggedIn",    true)
                                putString ("userUsername",  returnedUsername)
                                putString ("userEmail",     email)
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
    }
}
