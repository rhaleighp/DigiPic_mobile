package com.example.digipic

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AccountManagementActivity : AppCompatActivity() {

    private lateinit var firstNameText: TextView
    private lateinit var middleNameText: TextView
    private lateinit var lastNameText: TextView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var changePasswordBtn: Button
    private lateinit var deleteAccountBtn: Button
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_management)

        // Initialize Firebase + SharedPrefs
        db = FirebaseFirestore.getInstance()
        sharedPrefs = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)

        // Bind views
        firstNameText = findViewById(R.id.firstNameText)
        middleNameText = findViewById(R.id.middleNameText)
        lastNameText = findViewById(R.id.lastNameText)
        usernameText = findViewById(R.id.accountUsername)
        emailText = findViewById(R.id.accountEmail)
        changePasswordBtn = findViewById(R.id.changePasswordBtn)
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn)

        // Load user info
        loadUserInfo()

        // Buttons
        changePasswordBtn.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }


        deleteAccountBtn.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadUserInfo() {
        val username = sharedPrefs.getString("userUsername", null)
        if (username == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val user = snapshot.first()
                    firstNameText.text = user.getString("firstName") ?: "-"
                    middleNameText.text = user.getString("middleName") ?: "-"
                    lastNameText.text = user.getString("lastName") ?: "-"
                    usernameText.text = user.getString("username") ?: "-"
                    emailText.text = user.getString("email") ?: "-"
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Delete") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val username = sharedPrefs.getString("userUsername", null) ?: return

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val userDocId = snapshot.first().id
                    db.collection("users").document(userDocId)
                        .delete()
                        .addOnSuccessListener {
                            sharedPrefs.edit().clear().apply()
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, toLogin::class.java))
                            finish()
                        }
                }
            }
    }
}
