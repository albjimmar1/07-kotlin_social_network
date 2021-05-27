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
import com.example.myapp.adapter.MessageAdapter
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.*

class MessageChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var messageAdapter: MessageAdapter? = null
    private var mMessage: MutableList<Message>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val backBtn = findViewById<ImageView>(R.id.backImageView)
        val circleImageView = findViewById<ImageView>(R.id.circleImageView)
        val username = findViewById<TextView>(R.id.usernameTextView)
        val message = findViewById<EditText>(R.id.addMessageEditText)
        val sendMessageBtn = findViewById<ImageView>(R.id.sendMessageImageView)

        recyclerView = findViewById(R.id.messageChatRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.stackFromEnd = true
        //linearLayoutManager.scrollToPosition(-1)

        recyclerView?.layoutManager = linearLayoutManager
        mMessage = ArrayList()
        messageAdapter = this?.let {
            MessageAdapter(it, mMessage as ArrayList<Message>, false)
        }
        recyclerView?.adapter = messageAdapter

        recyclerView?.visibility = View.VISIBLE

        backBtn.setOnClickListener {
            onBackPressed()
        }

        var userVisited: User
        var userVisitedUid: String = ""
        reference.child("users").child(intent.getStringExtra("otherUserUid").toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        userVisitedUid = dataSnapshot.key.toString()
                        userVisited = User(
                            dataSnapshot.child("fullName").value.toString(),
                            dataSnapshot.child("username").value.toString(),
                            dataSnapshot.child("email").value.toString(),
                            dataSnapshot.child("birthDate").value.toString(),
                            dataSnapshot.child("password").value.toString(),
                            dataSnapshot.child("creationDate").value.toString(),
                            dataSnapshot.child("creationTime").value.toString()
                        )
                        userVisited.imageUrl = dataSnapshot.child("imageUrl").value.toString()
                        if (dataSnapshot.child("imageUrl").value.toString() != "")
                            Picasso.get().load(userVisited.imageUrl)
                                .placeholder(R.drawable.ic_baseline_person)
                                .into(circleImageView)
                        if (dataSnapshot.child("username").value.toString() != "")
                            username.text = dataSnapshot.child("username").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "MessageChatActivity",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })

        circleImageView.setOnClickListener {
            val autor: String = auth.currentUser!!.uid
            val receiver: String = userVisitedUid
            val c: Calendar = Calendar.getInstance()
            val day: String = c.get(Calendar.DAY_OF_MONTH).toString()
            val month: String = (c.get(Calendar.MONTH) + 1).toString()
            val year: String = c.get(Calendar.YEAR).toString()
            val hour: String = c.get(Calendar.HOUR_OF_DAY).toString()
            val minute: String = c.get(Calendar.MINUTE).toString()
            val second: String = c.get(Calendar.SECOND).toString()
            val date: String = "$day/$month/$year"
            val time: String = "$hour:$minute:$second"
            val millis: String = c.timeInMillis.toString()
            val visit = Visit(autor, receiver, date, time, millis)
            reference.child("users").child(userVisitedUid).child("visits")
                .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                .setValue(visit)

            this.startActivity(
                Intent(
                    this,
                    VisitedUserActivity::class.java
                ).putExtra("userUid", userVisitedUid)
            )
        }
        username.setOnClickListener {
            val autor: String = auth.currentUser!!.uid
            val receiver: String = userVisitedUid
            val c: Calendar = Calendar.getInstance()
            val day: String = c.get(Calendar.DAY_OF_MONTH).toString()
            val month: String = (c.get(Calendar.MONTH) + 1).toString()
            val year: String = c.get(Calendar.YEAR).toString()
            val hour: String = c.get(Calendar.HOUR_OF_DAY).toString()
            val minute: String = c.get(Calendar.MINUTE).toString()
            val second: String = c.get(Calendar.SECOND).toString()
            val date: String = "$day/$month/$year"
            val time: String = "$hour:$minute:$second"
            val millis: String = c.timeInMillis.toString()
            val visit = Visit(autor, receiver, date, time, millis)
            reference.child("users").child(userVisitedUid).child("visits")
                .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                .setValue(visit)

            startActivity(
                Intent(
                    this,
                    VisitedUserActivity::class.java
                ).putExtra("userUid", userVisitedUid)
            )
        }

        reference.child("users").child(auth.currentUser!!.uid).child("chats")
            .child(intent.getStringExtra("otherUserUid").toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mMessage?.clear()
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot2 in dataSnapshot.children) {
                            var messageVisitedUid: String = dataSnapshot.key.toString();
                            val message = Message(
                                snapshot2.child("autor").value.toString(),
                                snapshot2.child("receivor").value.toString(),
                                snapshot2.child("content").value.toString(),
                                snapshot2.child("date").value.toString(),
                                snapshot2.child("time").value.toString(),
                                snapshot2.child("momentMillis").value.toString()
                            )
                            if (message != null)
                                mMessage?.add(message)
                            messageAdapter?.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "MessageChatActivity",
                        "Failed to read event data.",
                        error.toException()
                    )
                }
            })

        sendMessageBtn.setOnClickListener {
            val fmessage: String = message.text.toString().trim()
            var error: Boolean = false
            val regex = Regex(pattern = """\w+(\,*\s*(\w+)*\.*)*""")
            if (TextUtils.isEmpty(fmessage)) {
                message.error = "Enter message."
                error = true
            } else if (fmessage.contains("_")) {
                message.error = "Message must not contain low bars."
                error = true
            } else if (!regex.matches(fmessage)) {
                message.error = "Message must only contain alphanumeric characters."
                error = true
            } else if (fmessage.length > 129) {
                message.error = "Message must be 128 characters or less"
                error = true
            }
            if (error) {
                return@setOnClickListener
            } else {
                val otherUserUid: String = intent.getStringExtra("otherUserUid").toString()
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
                val millis: String = c.timeInMillis.toString()
                val message =
                    Message(userId, otherUserUid, fmessage, creationDate, creationTime, millis)
                reference.child("users").child(userId).child("chats").child(otherUserUid)
                    .child(c.timeInMillis.toString()).setValue(message)
                reference.child("users").child(otherUserUid).child("chats").child(userId)
                    .child(millis).setValue(message)
            }
            message.setText("")
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