package com.example.myapp.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.ChatListActivity
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.example.myapp.adapter.NotificationAdapter
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InboxFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InboxFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var notificationAdapter: NotificationAdapter? = null
    private var mNotification: MutableList<Notification>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val view = inflater.inflate(R.layout.fragment_inbox, container, false)
        val chatsBtn = view.findViewById<ImageView>(R.id.chatsImageView)

        recyclerView = view.findViewById(R.id.notificationsRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        mNotification = ArrayList()
        notificationAdapter = context?.let {
            NotificationAdapter(it, mNotification as ArrayList<Notification>, true)
        }
        recyclerView?.adapter = notificationAdapter

        recyclerView?.visibility = View.VISIBLE

        chatsBtn.setOnClickListener {
            activity?.startActivity(
                Intent(
                    this@InboxFragment.context,
                    ChatListActivity::class.java
                )
            )
        }

        var user: User;
        var userUid: String = ""

        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mNotification?.clear()
                    if (dataSnapshot.hasChildren()) {
                        var notifications: MutableList<Notification> = mutableListOf()
                        userUid = dataSnapshot.key.toString()
                        user = User(
                            dataSnapshot.child("fullName").value.toString(),
                            dataSnapshot.child("username").value.toString(),
                            dataSnapshot.child("email").value.toString(),
                            dataSnapshot.child("birthDate").value.toString(),
                            dataSnapshot.child("password").value.toString(),
                            dataSnapshot.child("creationDate").value.toString(),
                            dataSnapshot.child("creationTime").value.toString()
                        )
                        user.userUid = userUid
                        user.imageUrl =
                            dataSnapshot.child("imageUrl").value.toString()
                        var follows: MutableList<Follow> = mutableListOf()
                        for (snapshot5 in dataSnapshot.child("followers").children) {
                            val follow = Follow(
                                snapshot5.child("autor").value.toString(),
                                snapshot5.child("receiver").value.toString(),
                                snapshot5.child("date").value.toString(),
                                snapshot5.child("time").value.toString(),
                                snapshot5.child("momentMillis").value.toString()
                            )
                            if (follow != null) {
                                follows?.add(follow)
                                val notification = Notification(
                                    "follow",
                                    "te ha seguido",
                                    follow.date,
                                    follow.time,
                                    follow.momentMillis
                                )
                                notification.userId = follow.autor
                                if (notification != null) {
                                    notifications?.add(notification)
                                    //mNotification?.add(notification)
                                    //notificationAdapter?.notifyDataSetChanged()
                                }
                            }
                        }

                        var posts: MutableList<Event> = mutableListOf()
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
                            event.imageAutor = snapshot2.child("imageAutor").value.toString()
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
                                if (comment != null) {
                                    comments?.add(comment)
                                    val notification = Notification(
                                        "comment",
                                        "ha comentado: " + comment.content,
                                        comment.date,
                                        comment.time,
                                        comment.momentMillis
                                    )
                                    notification.userId = comment.autor
                                    notification.eventId = event.eventUid
                                    notification.imageEvent = event.imageUrl
                                    if (notification != null) {
                                        notifications?.add(notification)
                                        //mNotification?.add(notification)
                                        //notificationAdapter?.notifyDataSetChanged()
                                    }
                                }
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
                                if (intention != null) {
                                    imgoingtos?.add(intention)
                                    val notification = Notification(
                                        "imgointgto",
                                        "va a ir a",
                                        intention.date,
                                        intention.time,
                                        intention.momentMillis
                                    )
                                    notification.userId = intention.autor
                                    notification.eventId = event.eventUid
                                    notification.imageEvent = event.imageUrl
                                    if (notification != null) {
                                        notifications?.add(notification)
                                        //mNotification?.add(notification)
                                        //notificationAdapter?.notifyDataSetChanged()
                                    }
                                }
                            }
                            event.imgoingtos = imgoingtos
                        }
                        // Sorted by date
                        mNotification?.addAll(notifications.sortedBy {
                            it.date!!.split('/')[2].toInt()+it.date!!.split('/')[1].toInt() * 100+it.date!!.split('/')[0].toInt()
                        })
                        notificationAdapter?.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "ProfileFragment",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InboxFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InboxFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}