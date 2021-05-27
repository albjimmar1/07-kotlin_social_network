package com.example.myapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.myapp.R
import com.example.myapp.adapter.PostAdapter
import com.example.myapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var eventAdapter: PostAdapter? = null
    private var mEvent: MutableList<Event>? = null

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

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val toFollowing = view.findViewById<TextView>(R.id.toFollowingTextView)
        val toMe = view.findViewById<TextView>(R.id.toMeTextView)

        recyclerView = view.findViewById(R.id.homeRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        val mSnapHelper: SnapHelper = PagerSnapHelper()
        mSnapHelper.attachToRecyclerView(recyclerView)

        mEvent = ArrayList()
        eventAdapter = context?.let {
            PostAdapter(it, mEvent as ArrayList<Event>, true)
        }
        recyclerView?.adapter = eventAdapter

        recyclerView?.visibility = View.VISIBLE

        var eventsFollowing: MutableList<Event> = mutableListOf()
        var eventsToMe: MutableList<Event> = mutableListOf()
        var usersFollowing: MutableList<String> = mutableListOf()

        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    usersFollowing?.clear()
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot2 in dataSnapshot.child("following").children) {
                            usersFollowing?.add(snapshot2.child("receiver").value.toString())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "HomeFragment",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })

        toFollowing.setOnClickListener {
            toFollowing.setTextColor(ContextCompat.getColor(this@HomeFragment.context!!, R.color.black))
            toMe.setTextColor(ContextCompat.getColor(this@HomeFragment.context!!, R.color.grey))
            mEvent?.clear()
            mEvent?.addAll(eventsFollowing)
            eventAdapter?.notifyDataSetChanged()
        }
        toMe.setOnClickListener {
            toFollowing.setTextColor(ContextCompat.getColor(this@HomeFragment.context!!, R.color.grey))
            toMe.setTextColor(ContextCompat.getColor(this@HomeFragment.context!!, R.color.black))
            mEvent?.clear()
            mEvent?.addAll(eventsToMe)
            eventAdapter?.notifyDataSetChanged()
        }

        reference.child("posts").limitToFirst(100)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mEvent?.clear()
                    for (snapshot in dataSnapshot.children) {
                        var eventShowedUid: String = snapshot.key.toString();
                        val event = Event(
                            snapshot.child("imageUrl").value.toString(),
                            snapshot.child("autor").value.toString(),
                            snapshot.child("title").value.toString(),
                            snapshot.child("description").value.toString(),
                            snapshot.child("category").value.toString(),
                            snapshot.child("city").value.toString(),
                            snapshot.child("location").value.toString(),
                            snapshot.child("creationDate").value.toString(),
                            snapshot.child("creationTime").value.toString(),
                            snapshot.child("startDate").value.toString(),
                            snapshot.child("endDate").value.toString(),
                            snapshot.child("startTime").value.toString(),
                            snapshot.child("endTime").value.toString(),
                            snapshot.child("capacity").value.toString().toIntOrNull(),
                            snapshot.child("capacityAvailable").value.toString().toIntOrNull(),
                            snapshot.child("accessPrice").value.toString().toDoubleOrNull()
                        )
                        event.eventUid = eventShowedUid
                        var comments: MutableList<Comment> = mutableListOf()
                        for (snapshot2 in snapshot.child("comments").children) {
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
                        var imgoingtos: MutableList<Intention> = mutableListOf()
                        for (snapshot3 in snapshot.child("imgoingtos").children) {
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
                        if (event != null) {
                            mEvent?.add(event)
                            eventsToMe?.add(event)
                            if (usersFollowing.contains(event.autor)) {
                                eventsFollowing?.add(event)
                            }
                        }
                    }
                    eventAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "HomeFragment",
                        "Failed to read posts data.",
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
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}