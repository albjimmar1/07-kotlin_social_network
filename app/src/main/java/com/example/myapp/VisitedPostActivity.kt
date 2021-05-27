package com.example.myapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.data.Comment
import com.example.myapp.data.Event
import com.example.myapp.data.Intention
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class VisitedPostActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visited_post)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference
        storage = FirebaseStorage.getInstance().reference

        val backBtn: ImageView = findViewById<ImageView>(R.id.backImageView)
        val image: ImageView = findViewById<ImageView>(R.id.imageView)
        val startDate: TextView = findViewById<TextView>(R.id.startDateTextView)
        val startTime: TextView = findViewById<TextView>(R.id.startTimeTextView)
        val capacityAvailable: TextView = findViewById<TextView>(R.id.capacityAvailableTextView)
        val accessPrice: TextView = findViewById<TextView>(R.id.accessPriceTextView)
        val locationImage: ImageView = findViewById<ImageView>(R.id.locationImageView)
        val circleImage: ImageView = findViewById<ImageView>(R.id.circleImageView)
        val weGoBtn: ImageView = findViewById<ImageView>(R.id.weGoImageView)
        val numberWeGo: TextView = findViewById<TextView>(R.id.numberWeGoTextView)
        val commentBtn: ImageView = findViewById<ImageView>(R.id.commentImageView)
        val numberComments: TextView = findViewById<TextView>(R.id.numberCommentsTextView)
        val editBtn: ImageView = findViewById<ImageView>(R.id.editImageView)
        val deleteBtn: ImageView = findViewById<ImageView>(R.id.deleteImageView)
        val username: TextView = findViewById<TextView>(R.id.usernameTextView)
        val title: TextView = findViewById<TextView>(R.id.titleTextView)
        val description: TextView = findViewById<TextView>(R.id.descriptionTextView)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        if (intent.getStringExtra("imageAutor").toString() != "")
            Picasso.get().load(intent.getStringExtra("imageAutor").toString())
                .placeholder(R.drawable.ic_baseline_person)
                .into(circleImage)

        commentBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CommentPostActivity::class.java
                ).putExtra("username", intent.getStringExtra("username").toString())
                    .putExtra("eventUid", intent.getStringExtra("eventUid").toString())
                    .putExtra("userUid", intent.getStringExtra("userUid").toString())
            )
        }

        var event: Event = Event()
        reference.child("posts").child(intent.getStringExtra("eventUid").toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        var eventShowedUid: String = dataSnapshot.key.toString();
                        event = Event(
                            dataSnapshot.child("imageUrl").value.toString(),
                            dataSnapshot.child("autor").value.toString(),
                            dataSnapshot.child("title").value.toString(),
                            dataSnapshot.child("description").value.toString(),
                            dataSnapshot.child("category").value.toString(),
                            dataSnapshot.child("city").value.toString(),
                            dataSnapshot.child("location").value.toString(),
                            dataSnapshot.child("creationDate").value.toString(),
                            dataSnapshot.child("creationTime").value.toString(),
                            dataSnapshot.child("startDate").value.toString(),
                            dataSnapshot.child("endDate").value.toString(),
                            dataSnapshot.child("startTime").value.toString(),
                            dataSnapshot.child("endTime").value.toString(),
                            dataSnapshot.child("capacity").value.toString().toIntOrNull(),
                            dataSnapshot.child("capacityAvailable").value.toString().toIntOrNull(),
                            dataSnapshot.child("accessPrice").value.toString().toDoubleOrNull()
                        )
                        if (event.imageUrl != "")
                            Picasso.get().load(event.imageUrl)
                                .placeholder(R.drawable.ic_baseline_person)
                                .into(image)
                        if (event.location == "")
                            locationImage.visibility = View.INVISIBLE
                        startDate.text = event.startDate
                        startTime.text = event.startTime
                        if (event.capacityAvailable == 0) {
                            capacityAvailable.text = "Agotado"
                        } else {
                            capacityAvailable.text = event.capacityAvailable.toString() + " plazas"
                        }
                        if (event.accessPrice == 0.0) {
                            accessPrice.text = "Gratis"
                        } else {
                            accessPrice.text = event.accessPrice.toString() + " €"
                        }
                        username.text = "@" + intent.getStringExtra("username").toString()
                        title.text = event.title.toString()
                        description.text = event.description.toString()
                        event.eventUid = eventShowedUid

                        var comments: MutableList<Comment> = mutableListOf()
                        for (snapshot2 in dataSnapshot.child("comments").children) {
                            val comment = Comment(
                                snapshot2.child("autor").value.toString(),
                                snapshot2.child("event").value.toString(),
                                snapshot2.child("content").value.toString(),
                                snapshot2.child("date").value.toString(),
                                snapshot2.child("time").value.toString(),
                                snapshot2.child("momentMillis").value.toString()
                            )
                            if (comment != null)
                                comments?.add(comment)
                        }
                        event.comments = comments
                        numberComments.text = event.comments!!.size.toString()
                        if (event.comments?.size!! > 0) {
                            commentBtn.setImageResource(R.drawable.ic_baseline_comment_black)
                            for (comment in event.comments!!) {
                                if (comment.autor == auth.currentUser!!.uid) {
                                    commentBtn.setImageResource(R.drawable.ic_baseline_comment_blue)
                                    break
                                }
                            }
                        }

                        var imgoingtos: MutableList<Intention> = mutableListOf()
                        for (snapshot3 in dataSnapshot.child("imgoingtos").children) {
                            val intention = Intention(
                                snapshot3.child("autor").value.toString(),
                                snapshot3.child("event").value.toString(),
                                snapshot3.child("date").value.toString(),
                                snapshot3.child("time").value.toString(),
                                snapshot3.child("momentMillis").value.toString()
                            )
                            if (intention != null)
                                imgoingtos?.add(intention)
                        }
                        event.imgoingtos = imgoingtos
                        numberWeGo.text = event.imgoingtos!!.size.toString()
                        if (event.imgoingtos?.size!! > 0) {
                            for (weGo in event.imgoingtos!!) {
                                if (weGo.autor == auth.currentUser!!.uid) {
                                    weGoBtn.setImageResource(R.drawable.ic_baseline_beenhere_red)
                                    break
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "VisitedPostActivity",
                        "Failed to read posts data.",
                        error.toException()
                    )
                }
            })

        if (intent.getStringExtra("userUid").toString() != auth.currentUser!!.uid) {
            editBtn.visibility = View.INVISIBLE
            deleteBtn.visibility = View.INVISIBLE
        }
        locationImage.setOnClickListener {
            if (event.location.toString() != "") {
                val location = LatLng(
                    event.location!!.split(",")[0].toDouble(),
                    event.location!!.split(",")[1].toDouble()
                )
                /*lastMarker = mMap.addMarker(
                    MarkerOptions().position(location2).title("Marker current location")
                )*/
                val strUri =
                    "geo:" + location.latitude.toString() + "," + location.longitude.toString() + "?z=18f"
                val intent: Intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(strUri)
                )
                startActivity(intent)
            }
        }

        editBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    EditPostActivity::class.java
                ).putExtra("username", intent.getStringExtra("username").toString())
                    .putExtra("eventUid", event.eventUid)
                    .putExtra("userUid", event.autor)
            )
        }

        deleteBtn.setOnClickListener {
            storage.child("postImages").child(event.autor.toString())
                .child(event.eventUid).delete()
                .addOnSuccessListener { Log.d("PostAdapter", "Success to delete Storage image.") }
                .addOnFailureListener { Log.w("PostAdapter", "Failed to delete Storage image.") }
            reference.child("posts").child(event.eventUid).setValue(null)
            reference.child("users").child(event.autor.toString())
                .child("posts").child(event.eventUid).setValue(null)
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