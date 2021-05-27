package com.example.myapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.adapter.PostAdapter2
import com.example.myapp.data.*
import com.example.myapp.fragments.InboxFragment
import com.example.myapp.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.*

class VisitedUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var eventAdapter: PostAdapter2? = null
    private var mEvent: MutableList<Event>? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visited_user)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val backBtn = findViewById<ImageView>(R.id.backImageView)
        val username = findViewById<TextView>(R.id.usernameTextView)
        val circleImageView = findViewById<ImageView>(R.id.circleImageView)
        val numberPosts = findViewById<TextView>(R.id.numberPosts)
        val numberFollowers = findViewById<TextView>(R.id.numberFollowers)
        val numberFollowing = findViewById<TextView>(R.id.numberFollowing)
        val numberWeGo = findViewById<TextView>(R.id.numberWeGo)
        val numberVisits = findViewById<TextView>(R.id.numberVisits)
        val fullName = findViewById<TextView>(R.id.fullNameTextView)
        val description = findViewById<TextView>(R.id.descriptionTextView)
        val followUnBtn = findViewById<Button>(R.id.followUnButton)
        val messageBtn = findViewById<Button>(R.id.messageButton)

        var userVisited: User;
        var userVisitedUid: String = ""

        recyclerView = findViewById(R.id.profileVisitedRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, 3, LinearLayout.VERTICAL, false)
        //gridLayoutManager.reverseLayout = true
        //gridLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = gridLayoutManager
        mEvent = ArrayList()
        eventAdapter = this?.let {
            PostAdapter2(it, mEvent as ArrayList<Event>, false)
        }
        recyclerView?.adapter = eventAdapter

        recyclerView?.visibility = View.VISIBLE

        backBtn.setOnClickListener {
            onBackPressed()
        }

        reference.child("users").child(intent.getStringExtra("userUid").toString())
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
                        userVisited.userUid = userVisitedUid
                        userVisited.imageUrl =
                            dataSnapshot.child("imageUrl").value.toString()
                        if (dataSnapshot.child("imageUrl").value.toString() != "")
                            Picasso.get().load(userVisited.imageUrl)
                                .placeholder(R.drawable.ic_baseline_person)
                                .into(circleImageView)
                        username.text = dataSnapshot.child("username").value.toString()
                        fullName.text = dataSnapshot.child("fullName").value.toString()
                        description.text = dataSnapshot.child("description").value.toString()
                        if (dataSnapshot.child("posts").childrenCount.toString() != "0")
                            numberPosts.text = dataSnapshot.child("posts").childrenCount.toString()
                        if (dataSnapshot.child("followers").childrenCount.toString() != "0")
                            numberFollowers.text =
                                dataSnapshot.child("followers").childrenCount.toString()
                        if (dataSnapshot.child("following").childrenCount.toString() != "0")
                            numberFollowing.text =
                                dataSnapshot.child("following").childrenCount.toString()
                        if (dataSnapshot.child("visits").childrenCount.toString() != "0")
                            numberVisits.text =
                                dataSnapshot.child("visits").childrenCount.toString()

                        for (snapshot2 in dataSnapshot.child("posts").children) {
                            var postVisitedUid: String = snapshot2.key.toString()
                            val event = Event(
                                snapshot2.child("imageUrl").value.toString(),
                                snapshot2.child("autor").value.toString(),
                                snapshot2.child("title").value.toString(),
                                snapshot2.child("description").value.toString(),
                                snapshot2.child("category").value.toString(),
                                snapshot2.child("city").value.toString(),
                                snapshot2.child("location").value.toString(),
                                snapshot2.child("creationDate").value.toString(),
                                snapshot2.child("creationTime").value.toString(),
                                snapshot2.child("startDate").value.toString(),
                                snapshot2.child("endDate").value.toString(),
                                snapshot2.child("startTime").value.toString(),
                                snapshot2.child("endTime").value.toString(),
                                snapshot2.child("capacity").value.toString().toIntOrNull(),
                                snapshot2.child("capacityAvailable").value.toString().toIntOrNull(),
                                snapshot2.child("accessPrice").value.toString().toDoubleOrNull()
                            )
                            event.eventUid = postVisitedUid
                            //event.imageAutor = snapshot2.child("imageAutor").value.toString()
                            var comments: MutableList<Comment> = mutableListOf()
                            for (snapshot3 in snapshot2.child("comments").children) {
                                val comment = Comment(
                                    snapshot3.child("autor").value.toString(),
                                    snapshot3.child("event").value.toString(),
                                    snapshot3.child("content").value.toString(),
                                    snapshot3.child("date").value.toString(),
                                    snapshot3.child("time").value.toString(),
                                    snapshot3.child("momentMillis").value.toString()
                                )
                                if (comment != null)
                                    comments?.add(comment)
                            }
                            event.comments = comments
                            var imgoingtos: MutableList<Intention> = mutableListOf()
                            for (snapshot4 in snapshot2.child("imgoingtos").children) {
                                val intention = Intention(
                                    snapshot4.child("autor").value.toString(),
                                    snapshot4.child("event").value.toString(),
                                    snapshot4.child("date").value.toString(),
                                    snapshot4.child("time").value.toString(),
                                    snapshot4.child("momentMillis").value.toString()
                                )
                                if (intention != null)
                                    imgoingtos?.add(intention)
                            }
                            event.imgoingtos = imgoingtos
                            // Calculation of visits
                            numberWeGo.text = (event.imgoingtos!!.size + numberWeGo.text.toString()
                                .toInt()).toString()
                            if (event != null) {
                                mEvent?.add(event)
                            }
                            eventAdapter?.notifyDataSetChanged()
                        }

                        reference.child("users").child(auth.currentUser!!.uid).child("following")
                            .child(userVisitedUid)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        followUnBtn.text = "Siguiendo"
                                    } else {
                                        followUnBtn.text = "Seguir"
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Failed to read value
                                    Log.w(
                                        "VisitedUserActivity",
                                        "Failed to read user data",
                                        error.toException()
                                    )
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "VisitedUserActivity",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })

        followUnBtn.setOnClickListener {
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
            val follow = Follow(autor, receiver, date, time, millis)
            if (followUnBtn.text == "Siguiendo") {
                reference.child("users").child(autor).child("following").child(receiver)
                    .setValue(null)
                reference.child("users").child(receiver).child("followers").child(autor)
                    .setValue(null)
                followUnBtn.text = "Seguir"
            } else {
                reference.child("users").child(autor).child("following").child(receiver)
                    .setValue(follow)
                reference.child("users").child(receiver).child("followers").child(autor)
                    .setValue(follow)
                followUnBtn.text = "Siguiendo"
            }
        }

        messageBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MessageChatActivity::class.java
                ).putExtra("otherUserUid", userVisitedUid)
            )
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