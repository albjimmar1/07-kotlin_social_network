package com.example.myapp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.CommentPostActivity
import com.example.myapp.EditPostActivity
import com.example.myapp.R
import com.example.myapp.VisitedUserActivity
import com.example.myapp.data.Event
import com.example.myapp.data.Intention
import com.example.myapp.data.Visit
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*


private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference
private lateinit var storage: StorageReference

class PostAdapter(
    private var mContext: Context,
    private var mPost: List<Event>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.activity_post, parent, false)
        return PostAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference
        storage = FirebaseStorage.getInstance().reference

        val event: Event = mPost[position]
        var eventAutorUsername: String = ""

        reference.child("users").child(event.autor.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    eventAutorUsername = dataSnapshot.child("username").value.toString()
                    event.imageAutor =
                        dataSnapshot.child("imageUrl").value.toString()
                    if (event.imageAutor != "") {
                        Picasso.get().load(event.imageAutor)
                            .placeholder(R.drawable.ic_baseline_person)
                            .into(holder.eventCircleImageView)
                    }
                    holder.eventUsername.text = "@" + eventAutorUsername
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "PostAdapter",
                        "Failed to read users data.",
                        error.toException()
                    )
                }
            })
        if (event.location == "") {
            holder.eventLocationImageView.visibility = View.INVISIBLE
        }
        holder.eventLocationImageView.setOnClickListener {
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
            mContext.startActivity(intent)
        }
        Picasso.get().load(event.imageUrl).placeholder(R.drawable.ic_baseline_person)
            .into(holder.eventImageView)
        holder.eventStartDateTextView.text = event.startDate
        holder.eventStartTimeTextView.text = event.startTime
        if (event.capacityAvailable.toString() == "0") {
            holder.eventCapacityAvailableTextView.text = "Agotado"
        } else {
            holder.eventCapacityAvailableTextView.text =
                event.capacityAvailable.toString() + " plazas libres"
        }
        if (event.accessPrice.toString() == "0.0") {
            holder.eventAccessPriceTextView.text = "Gratis"
        } else {
            holder.eventAccessPriceTextView.text = event.accessPrice.toString() + " €"
        }
        holder.eventNumberWeGoTextView.text = event.imgoingtos?.size.toString()
        var intention: Boolean = false
        for (imgoingto in event.imgoingtos!!) {
            if (imgoingto.autor == auth.currentUser!!.uid) {
                holder.eventWeGoImageView.setImageResource(R.drawable.ic_baseline_beenhere_red)
                intention = true
                break
            }
        }
        holder.eventNumberCommentsTextView.text = event.comments?.size.toString()
        if (event.comments?.size!! > 0) {
            holder.eventCommentImageView.setImageResource(R.drawable.ic_baseline_comment_black)
            for (comment in event.comments!!) {
                if (comment.autor == auth.currentUser!!.uid) {
                    holder.eventCommentImageView.setImageResource(R.drawable.ic_baseline_comment_blue)
                    break
                }
            }
        }
        holder.eventTitle.text = event.title
        holder.eventDescription.text = event.description
        holder.eventWeGoImageView.setOnClickListener {
            val autor: String = auth.currentUser!!.uid
            val eventUid: String = event.eventUid
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
            val imgoingto = Intention(autor, eventUid, date, time, millis)

            if (intention) {
                reference.child("posts").child(eventUid).child("imgoingtos").child(autor)
                    .setValue(null)
                reference.child("users").child(event.autor.toString()).child("posts")
                    .child(eventUid).child("imgoingtos").child(autor).setValue(null)
                //event.imgoingtos!!.remove(imgoingto)
                holder.eventWeGoImageView.setImageResource(R.drawable.ic_baseline_beenhere_black)
                //intention = false
            } else {
                reference.child("posts").child(eventUid).child("imgoingtos").child(autor)
                    .setValue(imgoingto)
                reference.child("users").child(event.autor.toString()).child("posts")
                    .child(eventUid).child("imgoingtos").child(autor).setValue(imgoingto)
                //event.imgoingtos!!.add(imgoingto)
                holder.eventWeGoImageView.setImageResource(R.drawable.ic_baseline_beenhere_red)
                //intention = true
            }
        }
        holder.eventNumberWeGoTextView.text = event.imgoingtos?.size.toString()
        holder.eventCommentImageView.setOnClickListener {
            mContext.startActivity(
                Intent(
                    mContext,
                    CommentPostActivity::class.java
                ).putExtra("username", eventAutorUsername).putExtra("eventUid", event.eventUid)
                    .putExtra("userUid", event.autor)
            )
        }
        holder.eventNumberCommentsTextView.text = event.comments?.size.toString()

        if (event.autor.toString() != auth.currentUser!!.uid) {
            holder.eventCircleImageView.setOnClickListener {
                val autor: String = auth.currentUser!!.uid
                val receiver: String = event.autor.toString()
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
                reference.child("users").child(event.autor.toString()).child("visits")
                    .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                    .setValue(visit)

                mContext.startActivity(
                    Intent(
                        mContext,
                        VisitedUserActivity::class.java
                    ).putExtra("userUid", event.autor)
                )
            }
            holder.eventUsername.setOnClickListener {
                val autor: String = auth.currentUser!!.uid
                val receiver: String = event.autor.toString()
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
                reference.child("users").child(event.autor.toString()).child("visits")
                    .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                    .setValue(visit)

                mContext.startActivity(
                    Intent(
                        mContext,
                        VisitedUserActivity::class.java
                    ).putExtra("userUid", event.autor)
                )
            }
        }
        if (event.autor.toString() != auth.currentUser!!.uid) {
            holder.eventEditImageView.visibility = View.INVISIBLE
            holder.eventDeleteImageView.visibility = View.INVISIBLE
        }
        holder.eventEditImageView.setOnClickListener {
            mContext.startActivity(
                Intent(
                    mContext,
                    EditPostActivity::class.java
                ).putExtra("username", eventAutorUsername).putExtra("eventUid", event.eventUid)
                    .putExtra("userUid", event.autor)
            )
        }
        holder.eventDeleteImageView.setOnClickListener {
            storage.child("postImages").child(event.autor.toString())
                .child(event.eventUid).delete()
                .addOnSuccessListener { Log.d("PostAdapter", "Success to delete Storage image.") }
                .addOnFailureListener { Log.w("PostAdapter", "Failed to delete Storage image.") }
            reference.child("posts").child(event.eventUid).setValue(null)
            reference.child("users").child(event.autor.toString())
                .child("posts").child(event.eventUid).setValue(null)
        }
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var eventImageView: ImageView = itemView.findViewById<ImageView>(R.id.imageView)
        var eventStartDateTextView: TextView =
            itemView.findViewById<TextView>(R.id.startDateTextView)
        var eventStartTimeTextView: TextView =
            itemView.findViewById<TextView>(R.id.startTimeTextView)
        var eventCapacityAvailableTextView: TextView =
            itemView.findViewById<TextView>(R.id.capacityAvailableTextView)
        var eventAccessPriceTextView: TextView =
            itemView.findViewById<TextView>(R.id.accessPriceTextView)
        var eventLocationImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.locationImageView)
        var eventCircleImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.circleImageView)
        var eventWeGoImageView: ImageView = itemView.findViewById<ImageView>(R.id.weGoImageView)
        var eventNumberWeGoTextView: TextView =
            itemView.findViewById<TextView>(R.id.numberWeGoTextView)
        var eventCommentImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.commentImageView)
        var eventNumberCommentsTextView: TextView =
            itemView.findViewById<TextView>(R.id.numberCommentsTextView)
        var eventEditImageView: ImageView = itemView.findViewById<ImageView>(R.id.editImageView)
        var eventDeleteImageView: ImageView = itemView.findViewById<ImageView>(R.id.deleteImageView)
        var eventUsername: TextView = itemView.findViewById<TextView>(R.id.usernameTextView)
        var eventTitle: TextView = itemView.findViewById<TextView>(R.id.titleTextView)
        var eventDescription: TextView = itemView.findViewById<TextView>(R.id.descriptionTextView)
    }
}