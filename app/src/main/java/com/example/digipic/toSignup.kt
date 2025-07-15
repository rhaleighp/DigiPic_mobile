package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class toSignup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup) // XML layout file

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Bind UI elements
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etMiddleName = findViewById<EditText>(R.id.etMiddleName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val tvLoginClickable = findViewById<TextView>(R.id.tvLoginClickable)

        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Basic validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val user = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "firstName" to firstName,
                            "middleName" to middleName,
                            "lastName" to lastName,
                            "email" to email,
                            "role" to "user",  // Default role
                            "createdAt" to Date()
                        )

                        // Save to Firestore
                        firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Sign-up successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, toLogin::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(this, "Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navigate to login
        tvLoginClickable.setOnClickListener {
            val intent = Intent(this, toLogin::class.java)
            startActivity(intent)
        }

    }
}
