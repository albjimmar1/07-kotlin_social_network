package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.adapter.CommentAdapter
import com.example.myapp.data.Comment
import com.example.myapp.data.Follow
import com.example.myapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class CommentPostActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var commentAdapter: CommentAdapter? = null
    private var mComment: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_post)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val backBtn = findViewById<ImageView>(R.id.backImageView)
        val commentsTextView = findViewById<TextView>(R.id.commentsTextView)
        val comment = findViewById<EditText>(R.id.addCommentEditText)
        val sendCommentBtn = findViewById<ImageView>(R.id.sendCommentImageView)

        recyclerView = findViewById(R.id.commentPostRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager
        mComment = ArrayList()
        commentAdapter = this?.let {
            CommentAdapter(it, mComment as ArrayList<Comment>, false)
        }
        recyclerView?.adapter = commentAdapter

        recyclerView?.visibility = View.VISIBLE

        backBtn.setOnClickListener {
            onBackPressed()
            /*startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(
                R.anim.translate_left_to_center_side,
                R.anim.translate_center_to_right_side
            )*/
        }

        sendCommentBtn.setOnClickListener {
            val fcomment: String = comment.text.toString().trim()
            var error: Boolean = false
            val regex = Regex(pattern = """\w+(\,*\s*(\w+)*\.*)*""")
            if (TextUtils.isEmpty(fcomment)) {
                comment.error = "Enter comment."
                error = true
            } else if (fcomment.contains("_")) {
                comment.error = "Comment must not contain low bars."
                error = true
            } else if (!regex.matches(fcomment)) {
                comment.error = "Comment must only contain alphanumeric characters."
                error = true
            } else if (fcomment.length > 129) {
                comment.error = "Comment must be 128 characters or less"
                error = true
            }
            if (error) {
                return@setOnClickListener
            } else {
                val eventAutor: String = intent.getStringExtra("userUid").toString()
                val userId: String = auth.currentUser!!.uid
                val eventId: String = intent.getStringExtra("eventUid").toString()
                val c: Calendar = Calendar.getInstance()
                val day: String = c.get(Calendar.DAY_OF_MONTH).toString()
                val month: String = (c.get(Calendar.MONTH) + 1).toString()
                val year: String = c.get(Calendar.YEAR).toString()
                val hour: String = c.get(Calendar.HOUR_OF_DAY).toString()
                val minute: String = c.get(Calendar.MINUTE).toString()
                val second: String = c.get(Calendar.SECOND).toString()
                val creationDate: String = "$day/$month/$year"
                val creationTime: String = "$hour:$minute:$second"
                val millis: String = c.timeInMillis.toString()
                val comment = Comment(userId, eventId, fcomment, creationDate, creationTime, millis)
                reference.child("posts").child(eventId).child("comments")
                    .child(userId + "(" + creationDate.replace("/", ":") + ";" + creationTime + ")")
                    .setValue(comment)
                reference.child("users").child(eventAutor).child("posts")
                    .child(eventId).child("comments")
                    .child(userId + "(" + creationDate.replace("/", ":") + ";" + creationTime + ")")
                    .setValue(comment)
            }
            comment.setText("")
        }

        reference.child("users").child(intent.getStringExtra("userUid").toString()).child("posts")
            .child(intent.getStringExtra("eventUid").toString()).child("comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mComment?.clear()
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot2 in dataSnapshot.children) {
                            var commentVisitedUid: String = dataSnapshot.key.toString();
                            val comment = Comment(
                                snapshot2.child("autor").value.toString(),
                                snapshot2.child("event").value.toString(),
                                snapshot2.child("content").value.toString(),
                                snapshot2.child("date").value.toString(),
                                snapshot2.child("time").value.toString(),
                                snapshot2.child("momentMillis").value.toString()
                            )
                            if (comment != null)
                                mComment?.add(comment)
                            commentAdapter?.notifyDataSetChanged()
                        }
                        //commentAdapter?.notifyDataSetChanged()
                        commentsTextView.text = mComment!!.size.toString() + " comentarios"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "CommentPostActivity",
                        "Failed to read event data.",
                        error.toException()
                    )
                }
            })
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