package com.example.myapp.adapter

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference

class MessageAdapter(
    private var mContext: Context,
    private var mMessage: List<Message>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val message: Message = mMessage[position]

        if (message.autor.toString() != auth.currentUser!!.uid) {
            holder.messageContentTextView.text = "other: " + message.content.toString()
            //holder.messageContentLinearLayout.gravity = Gravity.LEFT
            //holder.messageDateLinearLayout.gravity = Gravity.RIGHT
        } else {
            holder.messageContentTextView.text = "you: " + message.content.toString()
            //holder.messageContentLinearLayout.gravity = Gravity.RIGHT
            //holder.messageDateLinearLayout.gravity = Gravity.LEFT
        }

        holder.messageDateTextView.text = message.date.toString()
        holder.messageTimeTextView.text = message.time.toString()
    }

    override fun getItemCount(): Int {
        return mMessage.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageContentLinearLayout: LinearLayout =
            itemView.findViewById<LinearLayout>(R.id.messageLinearLayout)
        var messageDateLinearLayout: LinearLayout =
            itemView.findViewById<LinearLayout>(R.id.dateLinearLayout)
        var messageContentTextView: TextView = itemView.findViewById<TextView>(R.id.messageTextView)
        var messageDateTextView: TextView =
            itemView.findViewById<TextView>(R.id.dateTextView)
        var messageTimeTextView: TextView =
            itemView.findViewById<TextView>(R.id.timeTextView)
    }
}