package com.example.myapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.example.myapp.adapter.PostAdapter
import com.example.myapp.adapter.UserAdapter
import com.example.myapp.data.Event
import com.example.myapp.data.Follow
import com.example.myapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FollowersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

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

        val view = inflater.inflate(R.layout.fragment_followers, container, false)

        val backBtn = view.findViewById<ImageView>(R.id.backImageView)
        val followersTextView = view.findViewById<TextView>(R.id.followersTextView)

        recyclerView = view.findViewById(R.id.followersRecyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        mUser = ArrayList()
        userAdapter = context?.let {
            UserAdapter(it, mUser as ArrayList<User>, true)
        }
        recyclerView?.adapter = userAdapter

        backBtn.setOnClickListener {
            val profileFragment = ProfileFragment()
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fl_wrapper, profileFragment)
                //addToBackStack(null)
                commit()
            }
        }

        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mUser?.clear()
                    if (dataSnapshot.hasChildren()) {
                        var userVisitedUid: String = dataSnapshot.key.toString();
                        val user = User(
                            dataSnapshot.child("fullName").value.toString(),
                            dataSnapshot.child("username").value.toString(),
                            dataSnapshot.child("email").value.toString(),
                            dataSnapshot.child("birthDate").value.toString(),
                            dataSnapshot.child("password").value.toString(),
                            dataSnapshot.child("creationDate").value.toString(),
                            dataSnapshot.child("creationTime").value.toString()
                        )
                        user.userUid = userVisitedUid
                        user.imageUrl = dataSnapshot.child("imageUrl").value.toString()
                        user.description =
                            dataSnapshot.child("description").value.toString()

                        var followers: MutableList<Follow> = mutableListOf()
                        for (snapshot2 in dataSnapshot.child("followers").children) {
                            val follow = Follow(
                                snapshot2.child("autor").value.toString(),
                                snapshot2.child("receiver").value.toString(),
                                snapshot2.child("date").value.toString(),
                                snapshot2.child("time").value.toString(),
                                snapshot2.child("momentMillis").value.toString()
                            )
                            if (follow != null)
                                followers?.add(follow)

                            reference.child("users").child(follow.autor.toString())
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot2: DataSnapshot) {
                                        var userVisitedUid2: String = follow.autor.toString();
                                        val user = User(
                                            dataSnapshot2.child("fullName").value.toString(),
                                            dataSnapshot2.child("username").value.toString(),
                                            dataSnapshot2.child("email").value.toString(),
                                            dataSnapshot2.child("birthDate").value.toString(),
                                            dataSnapshot2.child("password").value.toString(),
                                            dataSnapshot2.child("creationDate").value.toString(),
                                            dataSnapshot2.child("creationTime").value.toString()
                                        )
                                        user.userUid = userVisitedUid2
                                        user.imageUrl =
                                            dataSnapshot2.child("imageUrl").value.toString()
                                        user.description =
                                            dataSnapshot2.child("description").value.toString()
                                        if (user != null) {
                                            mUser?.add(user)
                                        }
                                        userAdapter?.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Failed to read value
                                        Log.w(
                                            "FollowersFragment",
                                            "Failed to read user data.",
                                            error.toException()
                                        )
                                    }
                                })
                        }
                        user.followers = followers
                        followersTextView.text = user.followers!!.size.toString() + " seguidores"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "FollowersFragment",
                        "Failed to read user data.",
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
         * @return A new instance of fragment FollowersFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FollowersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}