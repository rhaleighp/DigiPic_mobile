package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class toSignup : AppCompatActivity() {

    private val client    = OkHttpClient()
    private val serverUrl = "http://127.0.0.1:5000/signup" // replace with your LAN IP

    private lateinit var etUsername: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        etUsername        = findViewById(R.id.etUsername)
        etFirstName       = findViewById(R.id.etFirstName)
        etMiddleName      = findViewById(R.id.etMiddleName)
        etLastName        = findViewById(R.id.etLastName)
        etEmail           = findViewById(R.id.etEmail)
        etPassword        = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignup         = findViewById(R.id.btnSignup)

        // Attach toggle logic to each password field
        setupPasswordToggle(etPassword)
        setupPasswordToggle(etConfirmPassword)

        btnSignup.setOnClickListener {
            val username  = etUsername.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middle    = etMiddleName.text.toString().trim()
            val lastName  = etLastName.text.toString().trim()
            val email     = etEmail.text.toString().trim()
            val pw        = etPassword.text.toString().trim()
            val cpw       = etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || pw.isEmpty() || cpw.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw != cpw) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("username",   username)
                put("firstName",  firstName)
                put("middleName", middle)
                put("lastName",   lastName)
                put("email",      email)
                put("password",   pw)
                put("isMobile",   true)
                put("isUser",     false)
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
                        Toast.makeText(this@toSignup, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    }

                override fun onResponse(call: Call, response: Response) {
                    val resp = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@toSignup, "Verification code sent!", Toast.LENGTH_SHORT).show()
                            startActivity(
                                Intent(this@toSignup, VerifyCodeActivity::class.java)
                                    .putExtra("email", email)
                            )
                            finish()
                        } else {
                            Toast.makeText(this@toSignup, "Signup failed: $resp", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }
    }

    /** Toggle show/hide on the EditText’s drawableEnd eye icon */
    private fun setupPasswordToggle(field: EditText) {
        field.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEndIndex = 2
                val dr = field.compoundDrawables[drawableEndIndex] ?: return@setOnTouchListener false
                val x  = event.x.toInt()
                val w  = field.width
                val padEnd = field.paddingEnd
                val dw = dr.intrinsicWidth
                if (x >= w - padEnd - dw) {
                    val currentlyVisible = field.transformationMethod == null
                    if (currentlyVisible) {
                        // hide
                        field.transformationMethod = PasswordTransformationMethod.getInstance()
                        field.setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            getDrawable(R.drawable.ic_visibility_off),
                            null
                        )
                    } else {
                        // show
                        field.transformationMethod = null
                        field.setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            getDrawable(R.drawable.ic_visibility),
                            null
                        )
                    }
                    // keep cursor at end
                    field.setSelection(field.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
