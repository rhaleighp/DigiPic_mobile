// QuizActivity.kt
package com.example.digipic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

// Make sure this is defined *before* the activity
data class QuizQuestion(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int
)

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var questionCounter: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView
    private lateinit var optionButtons: List<Button>

    private var questions = emptyList<QuizQuestion>()
    private var currentIndex = 0
    private var score = 0

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_content)

        questionText    = findViewById(R.id.questionText)
        questionCounter = findViewById(R.id.questionCounter)
        progressBar     = findViewById(R.id.progressBar)
        backButton      = findViewById(R.id.backButton)

        optionButtons = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )

        backButton.setOnClickListener { onBackPressed() }

        val quizId   = intent.getStringExtra("quizId")
        val moduleId = intent.getStringExtra("moduleId")
        val courseId = intent.getStringExtra("courseId")
        if (quizId == null || moduleId == null || courseId == null) {
            Toast.makeText(this, "Missing quiz/module/course ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupBottomNavigation()
        loadQuizQuestions(quizId)
    }

    private fun loadQuizQuestions(quizId: String) {
        firestore.collection("quizzes")
            .document(quizId)
            .collection("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                questions = snapshot.documents.mapNotNull { doc ->
                    val qText   = doc.getString("question") ?: return@mapNotNull null
                    val choices = (doc.get("choices") as? Map<*, *>)?.let {
                        listOf(
                            it["A"] as? String ?: "",
                            it["B"] as? String ?: "",
                            it["C"] as? String ?: "",
                            it["D"] as? String ?: ""
                        )
                    } ?: return@mapNotNull null
                    val correctIdx = when (doc.getString("correctAnswer")?.uppercase()) {
                        "A" -> 0; "B" -> 1; "C" -> 2; "D" -> 3; else -> -1
                    }
                    if (correctIdx < 0) return@mapNotNull null
                    QuizQuestion(qText, choices, correctIdx)
                }

                if (questions.isEmpty()) {
                    Toast.makeText(this, "No quiz questions found", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showQuestion()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun showQuestion() {
        if (currentIndex >= questions.size) {
            saveQuizResult()
            return
        }

        val q = questions[currentIndex]
        questionText.text    = q.question
        questionCounter.text = "Question ${currentIndex + 1} of ${questions.size}"
        progressBar.progress = ((currentIndex + 1).toFloat() / questions.size * 100).toInt()

        optionButtons.forEachIndexed { i, btn ->
            btn.text              = q.choices[i]
            btn.isEnabled         = true
            btn.setBackgroundResource(R.drawable.rounded_card)
            btn.setOnClickListener { handleAnswer(i) }
        }
    }

    private fun handleAnswer(selectedIndex: Int) {
        val correctIndex = questions[currentIndex].correctIndex
        if (selectedIndex == correctIndex) {
            score++
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show()
        }

        optionButtons.forEach { it.isEnabled = false }
        optionButtons[correctIndex].setBackgroundResource(R.drawable.tab_selected)

        optionButtons[0].postDelayed({
            currentIndex++
            showQuestion()
        }, 1000)
    }

    private fun saveQuizResult() {
        val quizId   = intent.getStringExtra("quizId")   ?: return
        val moduleId = intent.getStringExtra("moduleId") ?: return
        val courseId = intent.getStringExtra("courseId") ?: return

        val username = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
            .getString("userUsername", null) ?: return

        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { userDocs ->
                if (userDocs.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val userId = userDocs.first().id
                val result = hashMapOf(
                    "score"       to score,
                    "total"       to questions.size,
                    "quizId"      to quizId,
                    "moduleId"    to moduleId,
                    "courseId"    to courseId,
                    "completedAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("quizResults")
                    .document(quizId)
                    .set(result)
                    .addOnSuccessListener {
                        setResult(RESULT_OK)
                        Intent(this, QuizResultActivity::class.java).also { intent2 ->
                            intent2.putExtra("score", score)
                            intent2.putExtra("total", questions.size)
                            intent2.putExtra("courseId", courseId)
                            startActivity(intent2)
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save quiz result", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to find user", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageView>(R.id.mainGallery).setOnClickListener {
            startActivity(Intent(this, NewsFeedActivity::class.java))
        }
        findViewById<ImageView>(R.id.navLessons).setOnClickListener {
            startActivity(Intent(this, CourseActivity::class.java))
        }
        findViewById<ImageView>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
