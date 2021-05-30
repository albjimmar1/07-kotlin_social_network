package com.example.myapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.*
import com.example.myapp.R
import com.example.myapp.adapter.PostAdapter2
import com.example.myapp.data.Comment
import com.example.myapp.data.Event
import com.example.myapp.data.Intention
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var eventAdapter: PostAdapter2? = null
    private var mEvent: MutableList<Event>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("WrongConstant")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val username = view.findViewById<TextView>(R.id.usernameTextView)
        val optionsBtn = view.findViewById<ImageView>(R.id.optionsImageView)
        val circleImageView = view.findViewById<ImageView>(R.id.circleImageView)
        val numberPosts = view.findViewById<TextView>(R.id.numberPosts)
        val followersLinearLayout = view.findViewById<LinearLayout>(R.id.followersLinearLayout)
        val numberFollowers = view.findViewById<TextView>(R.id.numberFollowers)
        val followingLinearLayout = view.findViewById<LinearLayout>(R.id.followingLinearLayout)
        val numberFollowing = view.findViewById<TextView>(R.id.numberFollowing)
        val numberWeGo = view.findViewById<TextView>(R.id.numberWeGo)
        val numberVisits = view.findViewById<TextView>(R.id.numberVisits)
        val fullName = view.findViewById<TextView>(R.id.fullNameTextView)
        val description = view.findViewById<TextView>(R.id.descriptionTextView)
        val editProfileBtn = view.findViewById<Button>(R.id.editProfileButton)

        recyclerView = view.findViewById(R.id.profileRecyclerView)
        recyclerView?.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 3, GridLayout.VERTICAL, false)
        //gridLayoutManager.reverseLayout = true
        recyclerView?.layoutManager = gridLayoutManager
        mEvent = ArrayList()
        eventAdapter = context?.let {
            PostAdapter2(it, mEvent as ArrayList<Event>, true)
        }
        recyclerView?.adapter = eventAdapter

        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        if (dataSnapshot.child("imageUrl").value.toString() != "")
                            Picasso.get().load(dataSnapshot.child("imageUrl").value.toString())
                                .placeholder(R.drawable.ic_baseline_person)
                                .into(circleImageView)
                        username.text = dataSnapshot.child("username").value.toString()
                        fullName.text = dataSnapshot.child("fullName").value.toString()
                        description.text = dataSnapshot.child("description").value.toString()
                        if (dataSnapshot.child("posts").childrenCount.toString() != "0")
                            numberPosts.text =
                                dataSnapshot.child("posts").childrenCount.toString()
                        if (dataSnapshot.child("followers").childrenCount.toString() != "0")
                            numberFollowers.text =
                                dataSnapshot.child("followers").childrenCount.toString()
                        if (dataSnapshot.child("following").childrenCount.toString() != "0")
                            numberFollowing.text =
                                dataSnapshot.child("following").childrenCount.toString()
                        if (dataSnapshot.child("visits").childrenCount.toString() != "0")
                            numberVisits.text =
                                dataSnapshot.child("visits").childrenCount.toString()

                        var posts: MutableList<Event> = mutableListOf()
                        for (snapshot2 in dataSnapshot.child("posts").children) {
                            var postVisitedUid: String = snapshot2.key.toString()
                            val event = Event(
                                snapshot2.child("imageUrl").value.toString(),
                                snapshot2.child("autor").value.toString(),
                                snapshot2.child("title").value.toString(),
                                snapshot2.child("description").value.toString(),
                                snapshot2.child("category").value.toString(),
                                snapshot2.child("city").value.toString(),
                                snapshot2.child("location").value.toString(),
                                snapshot2.child("creationDate").value.toString(),
                                snapshot2.child("creationTime").value.toString(),
                                snapshot2.child("startDate").value.toString(),
                                snapshot2.child("endDate").value.toString(),
                                snapshot2.child("startTime").value.toString(),
                                snapshot2.child("endTime").value.toString(),
                                snapshot2.child("capacity").value.toString().toIntOrNull(),
                                snapshot2.child("capacityAvailable").value.toString().toIntOrNull(),
                                snapshot2.child("accessPrice").value.toString().toDoubleOrNull()
                            )
                            event.eventUid = postVisitedUid
                            event.imageAutor = snapshot2.child("imageAutor").value.toString()
                            var comments: MutableList<Comment> = mutableListOf()
                            for (snapshot3 in snapshot2.child("comments").children) {
                                val comment = Comment(
                                    snapshot3.child("autor").value.toString(),
                                    snapshot3.child("event").value.toString(),
                                    snapshot3.child("content").value.toString(),
                                    snapshot3.child("date").value.toString(),
                                    snapshot3.child("time").value.toString(),
                                    snapshot3.child("momentMillis").value.toString()
                                )
                                if (comment != null)
                                    comments?.add(comment)
                            }
                            event.comments = comments
                            var imgoingtos: MutableList<Intention> = mutableListOf()
                            for (snapshot4 in snapshot2.child("imgoingtos").children) {
                                val intention = Intention(
                                    snapshot4.child("autor").value.toString(),
                                    snapshot4.child("event").value.toString(),
                                    snapshot4.child("date").value.toString(),
                                    snapshot4.child("time").value.toString(),
                                    snapshot4.child("momentMillis").value.toString()
                                )
                                if (intention != null)
                                    imgoingtos?.add(intention)
                            }
                            event.imgoingtos = imgoingtos
                            // Calculation of visits
                            numberWeGo.text = (event.imgoingtos!!.size + numberWeGo.text.toString()
                                .toInt()).toString()
                            if (event != null) {
                                posts?.add(event)
                                //mEvent?.add(event)
                            }
                            //eventAdapter?.notifyDataSetChanged()
                        }
                        // Sorted by starDate
                        mEvent!!.addAll(posts.sortedBy {
                            it.startDate!!.split('/')[2].toInt()+it.startDate!!.split('/')[1].toInt() * 100+it.startDate!!.split('/')[0].toInt()
                        }.reversed())
                        eventAdapter?.notifyDataSetChanged()
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

        optionsBtn.setOnClickListener {
            val popup = PopupMenu(this@ProfileFragment.context, optionsBtn)
            popup.inflate(R.menu.options_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.ic_logOut -> {
                        auth.signOut()
                        startActivity(
                            Intent(
                                this@ProfileFragment.context,
                                AuthActivity::class.java
                            )
                        )
                        activity?.overridePendingTransition(
                            R.anim.translate_left_to_center_side,
                            R.anim.translate_center_to_right_side
                        )
                    }
                    R.id.ic_deleteAccount -> {
                        startActivity(
                            Intent(
                                this@ProfileFragment.context,
                                GoogleMapsActivity::class.java
                            )
                        )
                        /*auth.currentUser!!.delete()
                            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@ProfileFragment.context,
                                        "Your account has been deleted.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("ProfileFragment", "Your account has been to delete successfully.")
                                    startActivity(
                                        Intent(
                                            this@ProfileFragment.context,
                                            AuthActivity::class.java
                                        )
                                    )
                                    activity?.overridePendingTransition(
                                        R.anim.translate_left_to_center_side,
                                        R.anim.translate_center_to_right_side
                                    )
                                    activity?.finish()
                                }
                            }).addOnFailureListener(OnFailureListener { e ->
                                Log.e(
                                    "ProfileFragment",
                                    "Failed to delete your account.",
                                    e
                                )
                            })*/
                    }
                }
                true
            }
            popup.show()
        }

        followersLinearLayout.setOnClickListener {
            val followersFragment = FollowersFragment()
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fl_wrapper, followersFragment)
                //addToBackStack(null)
                commit()
            }
        }

        followingLinearLayout.setOnClickListener {
            val followingFragment = FollowingFragment()
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fl_wrapper, followingFragment)
                //addToBackStack(null)
                commit()
            }
        }

        editProfileBtn.setOnClickListener {
            activity?.startActivity(
                Intent(
                    this@ProfileFragment.context,
                    EditProfileActivity::class.java
                )
            )
            /*
            overridePendingTransition(R.anim.translate_right_to_center_side, R.anim.translate_center_to_left_side)
             */
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}