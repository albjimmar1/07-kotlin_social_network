package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        progressBar = findViewById<ProgressBar>(R.id.progressBar);
        // Firebase auth instance
        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.emailEditText)
        val sendBtn = findViewById<Button>(R.id.sendButton)
        val logInText = findViewById<TextView>(R.id.authTextView)

        sendBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val fEmail: String = email.text.toString().trim()
            var error: Boolean = false
            if (TextUtils.isEmpty(fEmail)) {
                email.error = "Email is required."
                error = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(fEmail).matches()) {
                email.error = "Email is not formatted correctly."
                error = true
            }
            if (error) {
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(fEmail).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("RecoverPassword", "sendPasswordResetEmail:success")
                    //val user = rAuth.currentUser
                    Toast.makeText(
                        baseContext, "Send mail successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                    startActivity(Intent(this, AuthActivity::class.java))
                    overridePendingTransition(
                        R.anim.slide_up_to_center,
                        R.anim.slide_center_to_down
                    )
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("RecoverPassword", "sendPasswordResetEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Send mail failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                    //updateUI(null)
                }
            }
        }

        logInText.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
            overridePendingTransition(R.anim.slide_up_to_center, R.anim.slide_center_to_down)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //updateUI(currentUser)
    }
}