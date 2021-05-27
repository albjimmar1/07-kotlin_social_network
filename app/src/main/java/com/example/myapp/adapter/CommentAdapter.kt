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
import com.example.myapp.R
import com.example.myapp.VisitedUserActivity
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.*

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference

class CommentAdapter(
    private var mContext: Context,
    private var mComment: List<Comment>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val comment: Comment = mComment[position]

        holder.commentContent.text = comment.content.toString()
        holder.commentDateTextView.text = comment.date.toString()
        holder.commentTimeTextView.text = comment.time.toString()

        if(comment.autor.toString() != auth.currentUser!!.uid){
            holder.commentCircleImageView.setOnClickListener {
                val autor: String = auth.currentUser!!.uid
                val receiver: String = comment.autor.toString()
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
                reference.child("users").child(comment.autor.toString()).child("visits")
                    .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                    .setValue(visit)

                mContext.startActivity(
                    Intent(
                        mContext,
                        VisitedUserActivity::class.java
                    ).putExtra("userUid", comment.autor)
                )
            }
            holder.commentUsername.setOnClickListener{
                val autor: String = auth.currentUser!!.uid
                val receiver: String = comment.autor.toString()
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
                reference.child("users").child(comment.autor.toString()).child("visits")
                    .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                    .setValue(visit)

                mContext.startActivity(
                    Intent(
                        mContext,
                        VisitedUserActivity::class.java
                    ).putExtra("userUid", comment.autor)
                )
            }
        }

        reference.child("users").child(comment.autor.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var userVisitedUid: String = dataSnapshot.key.toString()
                    val user = User(
                        dataSnapshot.child("fullName").value.toString(),
                        dataSnapshot.child("username").value.toString(),
                        dataSnapshot.child("email").value.toString(),
                        dataSnapshot.child("birthDate").value.toString(),
                        dataSnapshot.child("password").value.toString(),
                        dataSnapshot.child("creationDate").value.toString(),
                        dataSnapshot.child("creationTime").value.toString()
                    )
                    user.imageUrl = dataSnapshot.child("imageUrl").value.toString()
                    user.description =
                        dataSnapshot.child("description").value.toString()
                    if (user.imageUrl != "") {
                        Picasso.get().load(user.imageUrl)
                            .placeholder(R.drawable.ic_baseline_person)
                            .into(holder.commentCircleImageView)
                    }
                    holder.commentUsername.text = "@" + user.username
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "CommentAdapter",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var commentCircleImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.circleImageView)
        var commentUsername: TextView = itemView.findViewById<TextView>(R.id.usernameTextView)
        var commentContent: TextView = itemView.findViewById<TextView>(R.id.commentTextView)
        var commentDateTextView: TextView =
            itemView.findViewById<TextView>(R.id.dateTextView)
        var commentTimeTextView: TextView =
            itemView.findViewById<TextView>(R.id.timeTextView)
    }
}