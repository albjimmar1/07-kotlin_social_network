package com.example.myapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapp.data.*
import com.example.myapp.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    //private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        //window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val homeFragment = HomeFragment()
        val searchFragment = SearchFragment()
        val addFragment = AddFragment()
        val inboxFragment = InboxFragment()
        val profileFragment = ProfileFragment()

        val btnNav = findViewById<BottomNavigationView>(R.id.button_navigation)

        makeCurrentFragment(homeFragment)
        btnNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> makeCurrentFragment(homeFragment)
                R.id.ic_search -> makeCurrentFragment(searchFragment)
                R.id.ic_add -> startActivity(Intent(this, AddPostActivity::class.java))
                R.id.ic_inbox -> makeCurrentFragment(inboxFragment)
                R.id.ic_profile -> makeCurrentFragment(profileFragment)
            }
            true
        }
        /*
        var user: User;
        var userUid: String = ""

        val lastNotificationMillis: Long = Calendar.getInstance().timeInMillis
        val notificationLifetimeMillis: Long = 86400000
        val intentPrevious: Intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        if (/*lastNotificationMillis - notificationLifetimeMillis <= notification.momentMillis!!.toLong()*/true) {
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(this@MainActivity, 0, intentPrevious, 0)
            val CHANNEL_ID = "com.example.myapp.fragments"
            createNotificationChannel(
                CHANNEL_ID,
                "Not1",
                "Notification example",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val builder = NotificationCompat.Builder(
                this@MainActivity,
                CHANNEL_ID
            )
            builder.apply {
                setContentTitle("Notification")
                setContentText("Contenido de la notificación")
                setSmallIcon(R.mipmap.ic_launcher)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setContentIntent(pendingIntent)
                setAutoCancel(true)
            }
            var idNotification = Random().nextInt(1000000 - 0) + 0

            NotificationManagerCompat.from(this@MainActivity)
                .notify(idNotification, builder.build())
        }

        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
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
                                if (lastNotificationMillis - notificationLifetimeMillis <= notification.momentMillis!!.toLong()) {
                                    //notifications?.add(notification)
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
                                snapshot2.child("capacityAvailable").value.toString()
                                    .toIntOrNull(),
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
                                    if (lastNotificationMillis - notificationLifetimeMillis <= notification.momentMillis!!.toLong()) {
                                        //notifications?.add(notification)
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
                                    if (lastNotificationMillis - notificationLifetimeMillis <= notification.momentMillis!!.toLong()) {

                                    }
                                }
                            }
                            event.imgoingtos = imgoingtos
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "MainActivity",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })
    */
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            //addToBackStack(null)
            commit()
        }

    private fun createNotificationChannel(
        channel: String,
        name: String,
        description2: String,
        importance: Int
    ) {
        // The notification channel is created only if it is api 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channel, name, importance).apply {
                description = description2
            }
            // The channel is registered in the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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