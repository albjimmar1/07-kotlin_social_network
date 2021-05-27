package com.example.myapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.*
import com.example.myapp.R
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference
//private lateinit var storage: StorageReference

class NotificationAdapter(
    private var mContext: Context,
    private var mNotification: List<Notification>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_notification, parent, false)
        return NotificationAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference
        //storage = FirebaseStorage.getInstance().reference

        val notification: Notification = mNotification[position]

        if(notification.type == "follow"){
            holder.notificationImageView.visibility = View.INVISIBLE
        }

        if (notification.imageEvent != "")
            Picasso.get().load(notification.imageEvent).placeholder(R.drawable.ic_baseline_person)
                .into(holder.notificationImageView)
        holder.notificationContent.text = notification.content.toString()
        if (notification.imageUser != "")
            Picasso.get().load(notification.imageUser).placeholder(R.drawable.ic_baseline_person)
                .into(holder.notificationCircleImageView)
        holder.notificationDateTextView.text = notification.date.toString()
        holder.notificationTimeTextView.text = notification.time.toString()

        var user: User = User()
        var userUid: String = ""
        reference.child("users").child(notification.userId.toString())
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
                        if (user.imageUrl != "") {
                            Picasso.get().load(user.imageUrl)
                                .placeholder(R.drawable.ic_baseline_person)
                                .into(holder.notificationCircleImageView)
                        }
                        if (user.username != "")
                            holder.notificationUsername.text = "@" + user.username
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

        if (notification.userId.toString() != auth.currentUser!!.uid) {
            holder.notificationCircleImageView.setOnClickListener {
                val autor: String = auth.currentUser!!.uid
                val receiver: String = notification.userId.toString()
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
                reference.child("users").child(notification.userId.toString()).child("visits")
                    .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                    .setValue(visit)

                mContext.startActivity(
                    Intent(
                        mContext,
                        VisitedUserActivity::class.java
                    ).putExtra("userUid", notification.userId)
                )
            }
            holder.notificationUsername.setOnClickListener {
                val autor: String = auth.currentUser!!.uid
                val receiver: String = notification.userId.toString()
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
                reference.child("users").child(notification.userId.toString()).child("visits")
                    .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                    .setValue(visit)

                mContext.startActivity(
                    Intent(
                        mContext,
                        VisitedUserActivity::class.java
                    ).putExtra("userUid", notification.userId)
                )
            }
        }
        holder.notificationImageView.setOnClickListener {
            mContext.startActivity(
                Intent(
                    mContext,
                    VisitedPostActivity::class.java
                ).putExtra("eventUid", notification.eventId)
                    .putExtra("imageAutor", user.imageUrl)
                    .putExtra("username", user.username)
                    .putExtra("userUid", notification.userId)
            )
        }

    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var notificationCircleImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.circleImageView)
        var notificationUsername: TextView = itemView.findViewById<TextView>(R.id.usernameTextView)
        var notificationContent: TextView = itemView.findViewById<TextView>(R.id.contentTextView)
        var notificationImageView: ImageView = itemView.findViewById<ImageView>(R.id.postImageView)
        var notificationDateTextView: TextView =
            itemView.findViewById<TextView>(R.id.dateTextView)
        var notificationTimeTextView: TextView =
            itemView.findViewById<TextView>(R.id.timeTextView)
    }
}