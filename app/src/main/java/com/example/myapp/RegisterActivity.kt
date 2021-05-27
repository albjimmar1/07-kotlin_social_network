package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    //private lateinit var user: FirebaseUser
    //private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        progressBar = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.getReference()

        val fullName = findViewById<EditText>(R.id.fullNameEditText)
        val username = findViewById<EditText>(R.id.usernameEditText)
        val email = findViewById<EditText>(R.id.emailEditText)
        val birthDate = findViewById<EditText>(R.id.editTextDate)
        val password = findViewById<EditText>(R.id.passwordEditText)
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText)
        val signUpBtn = findViewById<Button>(R.id.signUpButton)
        val logInText = findViewById<TextView>(R.id.authTextView)

        birthDate.setOnClickListener {
            val datePicker =
                DatePickerFragment { day, month, year -> birthDate.setText("$day/" + (month + 1).toString() + "/$year") }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        signUpBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val fFullName: String = fullName.text.toString().trim()
            val fUsername: String = username.text.toString().trim()
            val fEmail: String = email.text.toString().trim().toLowerCase()
            //val formatDate = SimpleDateFormat("dd/MM/aaaa")
            //val fBirthDate: Date = formatDate.parse(birthDate.text.toString().trim())
            val fBirthDate: String = birthDate.text.toString().trim()
            val fPassword: String = password.text.toString().trim()
            val fConfirmPassword: String = confirmPassword.text.toString().trim()
            var error: Boolean = false
            val regex = Regex(pattern = """\w+(\,*\s*(\w+)*\.*)*""")
            val regex2 = Regex(pattern = """\w+(\s\w+)*""")
            val regex3 = Regex(pattern = """\w+""")
            if (!TextUtils.isEmpty(fFullName)) {
                if (fFullName.contains("_")) {
                    fullName.error = "FullName must not contain low bars."
                    error = true
                } else if (!regex2.matches(fFullName)) {
                    fullName.error = "FullName must only contain alphanumeric characters."
                    error = true
                } else if (fFullName.length > 64) {
                    fullName.error = "FullName must be 64 characters or less."
                    error = true
                }
            }
            if (TextUtils.isEmpty(fUsername)) {
                username.error = "Username is required."
                error = true
            } else if (fUsername.contains(" ")) {
                username.error = "Username must not contain spaces."
                error = true
            } else if (fUsername.contains("_")) {
                username.error = "Username must not contain low bars."
                error = true
            } else if (!regex3.matches(fUsername)) {
                username.error = "Username must only contain alphanumeric characters."
                error = true
            } else if (fUsername.length > 64) {
                username.error = "Username must be 64 characters or less."
                error = true
            } else if (fUsername != fUsername.toLowerCase()) {
                username.error = "Username must be lowercase."
                error = true
            } else {
                reference.child("users").orderByChild("username").equalTo(fUsername)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                username.error = "Username is already used."
                                error = true
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Log.w("RegisterActivity", "Failed to read nicks.", error.toException())
                        }
                    })
            }
            if (TextUtils.isEmpty(fEmail)) {
                email.error = "Email is required."
                error = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(fEmail).matches()) {
                email.error = "Email is not formatted correctly."
                error = true
            } else {
                reference.child("users").orderByChild("email").equalTo(fEmail)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                email.error = "Email is already used."
                                error = true
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Log.w("RegisterActivity", "Failed to read emails.", error.toException())
                        }
                    })
            }
            if (TextUtils.isEmpty(fPassword)) {
                password.error = "Password is required."
                error = true
            } else if (fPassword.length < 8) {
                password.error = "Password must be 8 characters or more"
                error = true
            }
            if (TextUtils.isEmpty(fConfirmPassword)) {
                confirmPassword.error = "Password confirmation is required."
                error = true
            } else if (!TextUtils.equals(fPassword, fConfirmPassword)) {
                password.error = "Passwords do not match."
                confirmPassword.error = "Passwords do not match."
                error = true
            }
            if (error) {
                progressBar.visibility = View.GONE
                return@setOnClickListener
            } else {
                // Register the user in Firebase
                auth.createUserWithEmailAndPassword(fEmail, fPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful && !error) {
                            // Sign in success, update UI with the signed-in user's information
                            writeNewUser(fFullName, fUsername, fEmail, fBirthDate, fPassword)
                            Log.d("RegisterActivity", "createUserWithEmail:success")
                            //user = auth.currentUser
                            Toast.makeText(
                                baseContext, "Sign up successful.",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = View.GONE
                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(
                                R.anim.translate_right_to_center_side,
                                R.anim.translate_center_to_left_side
                            )
                            //updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(
                                "RegisterActivity",
                                "createUserWithEmail:failure",
                                task.exception
                            )
                            Toast.makeText(
                                baseContext, "Sign up failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = View.GONE
                            //updateUI(null)
                        }
                    }
            }
        }
        logInText.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
            overridePendingTransition(R.anim.slide_up_to_center, R.anim.slide_center_to_down)
            finish()
        }
    }

    private fun writeNewUser(
        fullName: String?,
        username: String?,
        email: String?,
        birthDate: String?,
        password: String?
    ) {
        /*val userId: String = database.getReference("users").push().key.toString()*/
        val userId: String = auth.currentUser!!.uid
        val c: Calendar = Calendar.getInstance()
        val day: String = c.get(Calendar.DAY_OF_MONTH).toString()
        val month: String = (c.get(Calendar.MONTH) + 1).toString()
        val year: String = c.get(Calendar.YEAR).toString()
        val hour: String = c.get(Calendar.HOUR_OF_DAY).toString()
        val minute: String = c.get(Calendar.MINUTE).toString()
        val second: String = c.get(Calendar.SECOND).toString()
        val creationDate: String = "$day/$month/$year"
        val creationTime: String = "$hour:$minute:$second"
        val user = User(fullName, username, email, birthDate, password, creationDate, creationTime)
        reference.child("users").child(userId).setValue(user)
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