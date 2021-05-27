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
import com.example.myapp.data.User
import com.example.myapp.data.Visit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var reference: DatabaseReference

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_searched_user, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val user: User = mUser[position]
        if (user.imageUrl != "") {
            Picasso.get().load(user.imageUrl).placeholder(R.drawable.ic_baseline_person)
                .into(holder.userCircleImageView)
        }
        holder.userUsername.text = user.username
        holder.userFullName.text = user.fullName

        holder.containerLinearLayout.setOnClickListener {
            val autor: String = auth.currentUser!!.uid
            val receiver: String = user.userUid
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
            reference.child("users").child(user.userUid).child("visits")
                .child(auth.currentUser!!.uid + "-" + date.replace("/", "-") + "-" + time)
                .setValue(visit)

            mContext.startActivity(
                Intent(
                    mContext,
                    VisitedUserActivity::class.java
                ).putExtra("userUid", user.userUid)
            )
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userCircleImageView: ImageView =
            itemView.findViewById<ImageView>(R.id.circleImageView2)
        var userUsername: TextView = itemView.findViewById<TextView>(R.id.usernameTextView2)
        var userFullName: TextView = itemView.findViewById<TextView>(R.id.fullNameTextView2)
        var containerLinearLayout: LinearLayout = itemView.findViewById(R.id.containerLinearLayout)
    }
}