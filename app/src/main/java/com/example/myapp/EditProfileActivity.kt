package com.example.myapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage


class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference
        storage = FirebaseStorage.getInstance().reference

        val cancelEditBtn = findViewById<ImageView>(R.id.cancelImageView)
        val saveEditBtn = findViewById<ImageView>(R.id.saveImageView)
        val circleImageView = findViewById<ImageView>(R.id.circleImageView)
        val changeImageText = findViewById<TextView>(R.id.changeImageTextView)
        val fullName = findViewById<EditText>(R.id.fullNameEditText)
        val username = findViewById<EditText>(R.id.usernameEditText)
        val description = findViewById<EditText>(R.id.descriptionEditText)
        val email = findViewById<EditText>(R.id.emailEditText)
        val birthDate = findViewById<EditText>(R.id.dateEditText)

        cancelEditBtn.setOnClickListener {
            onBackPressed()
        }

        changeImageText.setOnClickListener {
            CropImage.activity().setAspectRatio(1, 1).start(this)
        }

        saveEditBtn.setOnClickListener {
            val fFullName: String = fullName.text.toString().trim()
            val fUsername: String = username.text.toString().trim()
            val fEmail: String = email.text.toString().trim().toLowerCase()
            //val formatDate = SimpleDateFormat("dd/MM/aaaa")
            //val fBirthDate: Date = formatDate.parse(birthDate.text.toString().trim())
            val fBirthDate: String = birthDate.text.toString().trim()
            val fDescription: String = description.text.toString().trim()
            var error: Boolean = false
            val regex = Regex(pattern = """\w+(\,*\s*(\w+)*\.*)*""")
            val regex2 = Regex(pattern = """\w+(\s\w+)*""")
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
            /*if (!TextUtils.isEmpty(fUsername)) {
                if (fUsername.contains(" ")) {
                    username.error = "Username must not contain spaces."
                    error = true
                } else if (fUsername.contains("_")) {
                    username.error = "Username must not contain low bars."
                    error = true
                } else if (!regex.matches(fUsername)) {
                    username.error = "Username must only contain alphanumeric characters."
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
                                Log.w("EditProfile", "Failed to read nicks.", error.toException())
                            }
                        })
                }
            }*/
            if (!TextUtils.isEmpty(fDescription)) {
                if (fDescription.contains("_")) {
                    description.error = "Description must not contain low bars."
                    error = true
                } else if (!regex.matches(fDescription)) {
                    description.error = "Description must only contain alphanumeric characters."
                    error = true
                } else if (fDescription.length < 8) {
                    description.error = "Description must be 8 characters or more."
                    error = true
                } else if (fDescription.length > 128) {
                    description.error = "Description must be 128 characters or less."
                    error = true
                }
            }
            if (error) {
                return@setOnClickListener
            } else {
                if (!TextUtils.isEmpty(fFullName))
                    reference.child("users").child(auth.currentUser!!.uid).child("fullName")
                        .setValue(fFullName)
                /*if (!TextUtils.isEmpty(fUsername))
                    reference.child("users").child(auth.currentUser!!.uid).child("username")
                        .setValue(fUsername)*/
                if (!TextUtils.isEmpty(fDescription))
                    reference.child("users").child(auth.currentUser!!.uid).child("description")
                        .setValue(fDescription)
                if (!TextUtils.isEmpty(fBirthDate))
                    reference.child("users").child(auth.currentUser!!.uid).child("birthDate")
                        .setValue(fBirthDate)
                if (imageUri != null) {
                    val fileRef =
                        storage.child("profileImages").child(auth.currentUser!!.uid)
                    var uploadTask = fileRef.putFile(imageUri!!)
                    uploadTask.continueWith { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        fileRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            fileRef.downloadUrl.addOnSuccessListener {
                                reference.child("users").child(auth.currentUser!!.uid)
                                    .child("imageUrl")
                                    .setValue(it.toString())
                            }.addOnFailureListener {

                            }
                            val downloadUri = task.result
                        } else {
                            // Handle failures
                        }
                    }
                }
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(
                    R.anim.translate_left_to_center_side,
                    R.anim.translate_center_to_right_side
                )
            }
        }

        birthDate.setOnClickListener {
            val datePicker =
                DatePickerFragment { day, month, year -> birthDate.setText("$day/" + (month + 1).toString() + "/$year") }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        if (dataSnapshot.child("imageUrl").value.toString() != "")
                            Picasso.get().load(dataSnapshot.child("imageUrl").value.toString())
                                .placeholder(R.drawable.ic_baseline_person)
                                .into(circleImageView)
                        if (dataSnapshot.child("fullName").value.toString() != "")
                            fullName.setText(dataSnapshot.child("fullName").value.toString())
                        if (dataSnapshot.child("username").value.toString() != "")
                            username.setText(dataSnapshot.child("username").value.toString())
                        if (dataSnapshot.child("description").value.toString() != "")
                            description.setText(dataSnapshot.child("description").value.toString())
                        if (dataSnapshot.child("email").value.toString() != "")
                            email.setText(dataSnapshot.child("email").value.toString())
                        if (dataSnapshot.child("birthDate").value.toString() != "")
                            birthDate.setText(dataSnapshot.child("birthDate").value.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "EditProfileActivity",
                        "Failed to read user data",
                        error.toException()
                    )
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                imageUri = result.uri
                val circleImageView = findViewById<ImageView>(R.id.circleImageView)
                circleImageView.setImageURI(imageUri)
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
        //updateUI(currentUser)
    }
}