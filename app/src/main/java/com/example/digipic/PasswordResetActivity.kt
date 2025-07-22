package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var resetPasswordBtn: Button
    private val client = OkHttpClient()
    val backendUrl = "http://10.0.2.2:5000/send-reset-code"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_reset)

        emailInput = findViewById(R.id.emailInput)
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn)

        resetPasswordBtn.setOnClickListener {
            val email = emailInput.text.toString().trim().lowercase()


            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("email", email)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(backendUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@PasswordResetActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@PasswordResetActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PasswordResetActivity, VerifyResetCodeActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@PasswordResetActivity, "Email not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}
