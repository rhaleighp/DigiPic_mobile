package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
            sendVerificationEmail()
        }

        // Start polling every 5 seconds
        startVerificationPolling()
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnSuccessListener {
                tvStatus.text = "A verification link has been sent to ${user.email}. Please check your inbox."
                Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                tvStatus.text = "Failed to send verification email: ${it.message}"
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_LONG).show()
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
