package com.example.digipic

import android.os.Bundle
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var questionCounter: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView
    private lateinit var optionButtons: List<Button>

    private var questions: List<QuizQuestion> = emptyList()
    private var currentIndex = 0
    private var score = 0

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_content)

        questionText = findViewById(R.id.questionText)
        questionCounter = findViewById(R.id.questionCounter)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)

        optionButtons = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )

        backButton.setOnClickListener { onBackPressed() }

        val quizId = intent.getStringExtra("quizId")
        if (quizId == null) {
            Toast.makeText(this, "Missing quiz ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadQuizQuestions(quizId)
    }

    private fun loadQuizQuestions(quizId: String) {
        firestore.collection("quizzes")
            .document(quizId)
            .collection("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                questions = snapshot.documents.mapNotNull { doc ->
                    val questionText = doc.getString("question") ?: return@mapNotNull null
                    val choicesMap = doc.get("choices") as? Map<*, *> ?: return@mapNotNull null
                    val correctLetter = doc.getString("correctAnswer") ?: return@mapNotNull null

                    val choices = listOf(
                        choicesMap["A"] as? String ?: "",
                        choicesMap["B"] as? String ?: "",
                        choicesMap["C"] as? String ?: "",
                        choicesMap["D"] as? String ?: ""
                    )

                    val correctIndex = when (correctLetter.uppercase()) {
                        "A" -> 0
                        "B" -> 1
                        "C" -> 2
                        "D" -> 3
                        else -> -1
                    }

                    if (correctIndex == -1) return@mapNotNull null

                    QuizQuestion(questionText, choices, correctIndex)
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
        questionText.text = q.question
        questionCounter.text = "Question ${currentIndex + 1} of ${questions.size}"
        progressBar.progress = ((currentIndex + 1).toFloat() / questions.size * 100).toInt()

        for (i in 0..3) {
            optionButtons[i].text = q.choices[i]
            optionButtons[i].isEnabled = true
            optionButtons[i].setBackgroundResource(R.drawable.rounded_card)

            optionButtons[i].setOnClickListener {
                handleAnswer(i)
            }
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
        val quizId = intent.getStringExtra("quizId") ?: return
        val moduleId = intent.getStringExtra("moduleId") ?: return
        val courseId = intent.getStringExtra("courseId") ?: return

        val sharedPref = getSharedPreferences("DigiPicPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("userUsername", null) ?: return

        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    val userId = userDocs.first().id

                    val result = hashMapOf(
                        "score" to score,
                        "total" to questions.size,
                        "quizId" to quizId,
                        "moduleId" to moduleId,
                        "courseId" to courseId,
                        "completedAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(userId)
                        .collection("quizResults")
                        .document(quizId)
                        .set(result)
                        .addOnSuccessListener {
                            val intent = Intent(this, QuizResultActivity::class.java)
                            intent.putExtra("score", score)
                            intent.putExtra("total", questions.size)
                            intent.putExtra("courseId", courseId)
                            startActivity(intent)
                            finish()

                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save quiz result", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    data class QuizQuestion(
        val question: String,
        val choices: List<String>,
        val correctIndex: Int
    )
}
