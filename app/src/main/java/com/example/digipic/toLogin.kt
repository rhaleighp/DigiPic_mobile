package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class toLogin : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.getStartedButton)
        val signupText = findViewById<TextView>(R.id.tvSignup)

        // Navigate to Sign Up screen
        signupText.setOnClickListener {
            val intent = Intent(this, toSignup::class.java)
            startActivity(intent)
        }

        // Handle login
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim().lowercase()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("LOGIN_DEBUG", "Entered username: $username")
            Log.d("LOGIN_DEBUG", "Entered password: $password")

            // Step 1: Find email from Firestore based on username
            firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "Username not found.", Toast.LENGTH_SHORT).show()
                        Log.d("LOGIN_DEBUG", "No Firestore match for username: $username")
                        return@addOnSuccessListener
                    }

                    for (doc in documents) {
                        Log.d("LOGIN_DEBUG", "Match: ${doc.getString("username")} -> ${doc.getString("email")}")
                    }

                    val email = documents.first().getString("email")
                    if (email.isNullOrEmpty()) {
                        Toast.makeText(this, "Email not linked to username.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    Log.d("LOGIN_DEBUG", "Resolved email: $email")

                    // Step 2: Login with email and password
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                Log.d("LOGIN_DEBUG", "Firebase login failed: ${task.exception?.message}")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to find user: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.d("LOGIN_DEBUG", "Firestore error: ${e.message}")
                }
        }
    }
}
