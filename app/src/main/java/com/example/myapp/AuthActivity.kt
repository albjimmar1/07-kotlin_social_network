package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.example.myapp.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import java.util.*

class AuthActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    //private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    //private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //progressBar = findViewById<ProgressBar>(R.id.progressBar)
        // Firebase auth instance
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val email = findViewById<EditText>(R.id.emailEditText)
        val password = findViewById<EditText>(R.id.passwordEditText)
        val logInBtn = findViewById<Button>(R.id.logInButton)
        val resetPassword = findViewById<TextView>(R.id.resetPasswordTextView)
        val googleButton = findViewById<TextView>(R.id.googleButton)
        val signUpText = findViewById<TextView>(R.id.registerTextView)

        logInBtn.setOnClickListener {
            //progressBar.visibility = View.VISIBLE
            val fEmail: String = email.text.toString().trim().toLowerCase()
            val fPassword: String = password.text.toString().trim()
            var error: Boolean = false
            if (TextUtils.isEmpty(fEmail)) {
                email.error = "Enter email."
                error = true
            } else {
                reference.child("users").orderByChild("email").equalTo(fEmail)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                email.error = "Unknown email."
                                error = true
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Log.w(
                                "AuthActivity",
                                "Failed to read emails.",
                                error.toException()
                            )
                        }
                    })
            }
            if (TextUtils.isEmpty(fPassword)) {
                password.error = "Enter password."
                error = true
            } else if (!TextUtils.isEmpty(fEmail)) {
                reference.child("users").orderByChild("email").equalTo(fEmail)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.children.first()
                                    .child("password").value != fPassword
                            ) {
                                password.error = "Incorrect password."
                                error = true
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Log.w(
                                "AuthActivity",
                                "Failed to read passwords.",
                                error.toException()
                            )
                        }
                    })
            }
            if (error) {
                //progressBar.visibility = View.GONE
                return@setOnClickListener
            }
            // Authenticate the user
            auth.signInWithEmailAndPassword(fEmail, fPassword).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("AuthActivity", "signInWithEmailAndPassword:success")
                    //val user = aAuth.currentUser
                    //progressBar.visibility = View.GONE
                    Toast.makeText(
                        baseContext, "Log in successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(
                        R.anim.translate_right_to_center_side,
                        R.anim.translate_center_to_left_side
                    )
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        "AuthActivity",
                        "signInWithEmailAndPassword:failure",
                        task.exception
                    )
                    //progressBar.visibility = View.GONE
                    Toast.makeText(
                        baseContext, "Log in failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }
            }
        }
        resetPassword.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
            overridePendingTransition(R.anim.slide_down_to_center, R.anim.slide_center_to_up)
            finish()
        }
        googleButton.setOnClickListener {
            //progressBar.visibility = View.VISIBLE
            googleSignInClient.signOut()
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        signUpText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_down_to_center, R.anim.slide_center_to_up)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("AuthActivity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!, account.email.toString())
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    //progressBar.visibility = View.GONE
                    Log.w("AuthActivity", "firebaseAuthWithGoogle:failure", e)
                    Toast.makeText(
                        baseContext, "Log in with google failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                //progressBar.visibility = View.GONE
                //Log.w("AuthActivity", exception.toString());
                Toast.makeText(
                    baseContext, "Log in with google failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, emailAccount: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("AuthActivity", "signInWithCredential:success")
                    //progressBar.visibility = View.GONE
                    reference.child("users").orderByChild("email").equalTo(emailAccount)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (!dataSnapshot.hasChildren()) {
                                    writeNewUserWithGoogle(
                                        emailAccount.split("@")[0],
                                        emailAccount.split("@")[0] + "_",
                                        emailAccount,
                                        "",
                                        ""
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w(
                                    "AuthActivity",
                                    "Failed to read emails.",
                                    error.toException()
                                )
                            }
                        })
                    Toast.makeText(
                        baseContext, "Log in with google successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(
                        R.anim.translate_right_to_center_side,
                        R.anim.translate_center_to_left_side
                    )
                    //val user = auth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("AuthActivity", "signInWithCredential:failure", task.exception)
                    //Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    //updateUI(null)
                    //progressBar.visibility = View.GONE
                    Toast.makeText(
                        baseContext, "Log in with google failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun writeNewUserWithGoogle(
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