package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.adapter.ChatListAdapter
import com.example.myapp.adapter.CommentAdapter
import com.example.myapp.adapter.PostAdapter2
import com.example.myapp.data.Chat
import com.example.myapp.data.Comment
import com.example.myapp.data.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var chatListAdapter: ChatListAdapter? = null
    private var mChat: MutableList<Chat>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val backBtn = findViewById<ImageView>(R.id.backImageView)

        recyclerView = findViewById(R.id.chatListRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager
        mChat = ArrayList()
        chatListAdapter = this?.let {
            ChatListAdapter(it, mChat as ArrayList<Chat>, false)
        }
        recyclerView?.adapter = chatListAdapter

        recyclerView?.visibility = View.VISIBLE

        reference.child("users").child(auth.currentUser!!.uid).child("chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mChat?.clear()
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot2 in dataSnapshot.children) {
                            var chatVisitedUid: String = snapshot2.key.toString()
                            val chat = Chat(
                                chatVisitedUid,
                                auth.currentUser!!.uid,
                                snapshot2.children.last().child("content").value.toString(),
                                snapshot2.children.last().child("date").value.toString(),
                                snapshot2.children.last().child("time").value.toString(),
                                snapshot2.children.last().child("momentMillis").value.toString()
                            )
                            if (chat != null)
                                mChat?.add(chat)
                            chatListAdapter?.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "ChatListActivity",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })

        backBtn.setOnClickListener {
            onBackPressed()
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