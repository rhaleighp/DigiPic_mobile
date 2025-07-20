package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class VerifyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvStatus: TextView
    private lateinit var btnResend: Button

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify)

        auth = FirebaseAuth.getInstance()

        tvStatus = findViewById(R.id.tvStatus)
        btnResend = findViewById(R.id.btnResend)

        // Send initial verification email
        sendVerificationEmail()

        // Resend button
        btnResend.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d("VerifyDebug", "Resending to: ${user.email}")
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        Log.d("VerifyDebug", "Verification email sent successfully.")
                        Toast.makeText(this, "Sent to ${user.email}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("VerifyDebug", "Failed to send email: ${e.message}", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Log.e("VerifyDebug", "No authenticated user.")
            }
        }

        // Start polling every 5 seconds
        startVerificationPolling()
    }

    private fun sendVerificationEmail() {
        Log.d("VerifyActivity", "sendVerificationEmail() called")

        val user = auth.currentUser
        if (user == null) {
            Log.e("VerifyActivity", "No authenticated user found.")
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable the resend button to avoid multiple requests
        btnResend.isEnabled = false

        user.sendEmailVerification()
            .addOnSuccessListener {
                Log.d("VerifyActivity", "Verification email sent successfully")
                tvStatus.text = "A verification link has been sent to ${user.email}. Please check your inbox."
                Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
                btnResend.isEnabled = true
            }
            .addOnFailureListener { e ->
                Log.e("VerifyActivity", "Failed to send verification email", e)
                tvStatus.text = "Failed to send verification email: ${e.message}"
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_LONG).show()
                btnResend.isEnabled = true
            }
    }

    private fun startVerificationPolling() {
        checkRunnable = object : Runnable {
            override fun run() {
                auth.currentUser?.reload()?.addOnSuccessListener {
                    if (auth.currentUser?.isEmailVerified == true) {
                        Toast.makeText(this@VerifyActivity, "Email verified!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@VerifyActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        handler.postDelayed(this, 5000) // Retry in 5 seconds
                    }
                }?.addOnFailureListener {
                    handler.postDelayed(this, 5000)
                }
            }
        }

        handler.post(checkRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable) // Stop polling if activity is destroyed
    }
}
