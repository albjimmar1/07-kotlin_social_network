package com.example.myapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.VisitedPostActivity
import com.example.myapp.data.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference

class PostAdapter2(
    private var mContext: Context,
    private var mPost: List<Event>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<PostAdapter2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter2.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_post_profile, parent, false)
        return PostAdapter2.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostAdapter2.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val event: Event = mPost[position]
        var eventAutorUsername: String = ""

        holder.mainRelativeLayout.setOnClickListener {
            mContext.startActivity(
                Intent(
                    mContext,
                    VisitedPostActivity::class.java
                ).putExtra("eventUid", event.eventUid)
                    .putExtra("imageAutor", event.imageAutor)
                    .putExtra("username", eventAutorUsername)
                    .putExtra("userUid", event.autor.toString())
            )
        }

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
                        "PostAdapter2",
                        "Failed to read users data.",
                        error.toException()
                    )
                }
            })

        Picasso.get().load(event.imageUrl).placeholder(R.drawable.ic_baseline_person)
            .into(holder.eventImageView)
        holder.eventStartDateTextView.text = event.startDate
        holder.eventStartTimeTextView.text = event.startTime
        if (event.capacityAvailable.toString() == "0") {
            holder.eventCapacityAvailableTextView.text = "Agotado"
        } else {
            holder.eventCapacityAvailableTextView.text = event.capacityAvailable.toString()
        }
        if (event.accessPrice.toString() == "0.0") {
            holder.eventAccessPriceTextView.text = "Gratis"
        } else {
            holder.eventAccessPriceTextView.text = event.accessPrice.toString() + " €"
        }
        holder.eventNumberWeGoTextView.text = event.imgoingtos?.size.toString()
        for (imgoingto in event.imgoingtos!!) {
            if (imgoingto.autor == auth.currentUser!!.uid) {
                holder.eventNumberWeGoTextView.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.red
                    )
                )
                break
            }
        }
        holder.eventNumberCommentsTextView.text = event.comments?.size.toString()
        if (event.comments?.size!! > 0) {
            for (comment in event.comments!!) {
                if (comment.autor == auth.currentUser!!.uid) {
                    holder.eventNumberCommentsTextView.setTextColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.blue
                        )
                    )
                    break
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mainRelativeLayout: RelativeLayout =
            itemView.findViewById<RelativeLayout>(R.id.mainRelativeLayout)
        var eventImageView: ImageView = itemView.findViewById<ImageView>(R.id.imageView)
        var eventStartDateTextView: TextView =
            itemView.findViewById<TextView>(R.id.startDateTextView)
        var eventStartTimeTextView: TextView =
            itemView.findViewById<TextView>(R.id.startTimeTextView)
        var eventCapacityAvailableTextView: TextView =
            itemView.findViewById<TextView>(R.id.capacityAvailableTextView)
        var eventAccessPriceTextView: TextView =
            itemView.findViewById<TextView>(R.id.accessPriceTextView)
        var eventCircleImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.circleImageView)
        var eventNumberWeGoTextView: TextView =
            itemView.findViewById<TextView>(R.id.numberWeGoTextView)
        var eventNumberCommentsTextView: TextView =
            itemView.findViewById<TextView>(R.id.numberCommentsTextView)
        var eventUsername: TextView = itemView.findViewById<TextView>(R.id.usernameTextView)
    }
}