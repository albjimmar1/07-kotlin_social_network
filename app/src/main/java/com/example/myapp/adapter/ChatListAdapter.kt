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
import com.example.myapp.CommentPostActivity
import com.example.myapp.MessageChatActivity
import com.example.myapp.R
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference

class ChatListAdapter(
    private var mContext: Context,
    private var mChat: List<Chat>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListAdapter.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val chat: Chat = mChat[position]

        reference.child("users").child(chat.autor.toString())
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
                            .into(holder.chatCircleImageView)
                    }
                    if (user.fullName != "") {
                        holder.chatFullNameTextView.text = user.fullName
                    } else {
                        holder.chatFullNameTextView.text = user.username
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "ChatListAdapter",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })
        holder.chatLastMessageTextView.text = chat.lastMessage
        holder.chatContanierLinearLayout.setOnClickListener {
            mContext.startActivity(
                Intent(
                    mContext,
                    MessageChatActivity::class.java
                ).putExtra("otherUserUid", chat.autor)
            )
        }
    }

    override fun getItemCount(): Int {
        return mChat.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var chatCircleImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.circleImageView)
        var chatFullNameTextView: TextView = itemView.findViewById<TextView>(R.id.fullNameTextView)
        var chatLastMessageTextView: TextView =
            itemView.findViewById<TextView>(R.id.lastMessageTextView)
        var chatContanierLinearLayout: LinearLayout =
            itemView.findViewById<LinearLayout>(R.id.containerLinearLayout)
    }
}