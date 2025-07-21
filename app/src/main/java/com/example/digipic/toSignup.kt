package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull // ✅ FIX THIS LINE
import org.json.JSONObject
import java.io.IOException

class toSignup : AppCompatActivity() {

    private val client = OkHttpClient()
    private val serverUrl = "http://10.0.2.2:5000/signup" // use your server IP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etMiddleName = findViewById<EditText>(R.id.etMiddleName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        btnSignup.setOnClickListener {
            val json = JSONObject().apply {
                put("username", etUsername.text.toString())
                put("firstName", etFirstName.text.toString())
                put("middleName", etMiddleName.text.toString())
                put("lastName", etLastName.text.toString())
                put("email", etEmail.text.toString())
                put("password", etPassword.text.toString())
                put("isMobile", true)
            }

            val body = RequestBody.create(
                "application/json".toMediaTypeOrNull(), json.toString()
            )

            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@toSignup, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@toSignup, "Verification code sent!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@toSignup, VerifyCodeActivity::class.java)
                            intent.putExtra("email", etEmail.text.toString())
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@toSignup, "Signup error: $responseBody", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }
    }
}
