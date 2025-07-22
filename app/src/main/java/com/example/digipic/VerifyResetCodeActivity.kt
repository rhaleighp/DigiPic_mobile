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

class VerifyResetCodeActivity : AppCompatActivity() {

    private lateinit var etVerificationCode: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnVerifyCode: Button
    private val client = OkHttpClient()

    private val backendUrl = "http://10.0.2.2:5000/verify-reset-code" // Update if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_reset_code)

        val email = intent.getStringExtra("email") ?: ""

        etVerificationCode = findViewById(R.id.etVerificationCode)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnVerifyCode = findViewById(R.id.btnVerifyCode)

        btnVerifyCode.setOnClickListener {
            val code = etVerificationCode.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            if (code.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("email", email)
                put("code", code)
                put("newPassword", newPassword)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(backendUrl)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@VerifyResetCodeActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@VerifyResetCodeActivity, "Password updated successfully", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@VerifyResetCodeActivity, toLogin::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@VerifyResetCodeActivity, "Invalid code or failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}
