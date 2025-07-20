package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull


class VerifyCodeActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val verifyUrl = "http://10.0.2.2:5000/verify-code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_code)

        val etCode = findViewById<EditText>(R.id.etVerificationCode)
        val btnVerify = findViewById<Button>(R.id.btnVerifyCode)
        val email = intent.getStringExtra("email")

        btnVerify.setOnClickListener {
            val json = JSONObject().apply {
                put("email", email)
                put("code", etCode.text.toString())
            }

            val body = RequestBody.create(
                "application/json".toMediaTypeOrNull(), json.toString()
            )

            val request = Request.Builder()
                .url(verifyUrl)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@VerifyCodeActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@VerifyCodeActivity, "Verified successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@VerifyCodeActivity, toLogin::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@VerifyCodeActivity, "Error: $responseBody", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }
    }
}
