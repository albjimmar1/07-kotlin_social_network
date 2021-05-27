package com.example.myapp.fragments

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapter.PostAdapter2
import com.example.myapp.adapter.UserAdapter
import com.example.myapp.data.Comment
import com.example.myapp.data.Event
import com.example.myapp.data.Intention
import com.example.myapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    private var cityAdapter: ArrayAdapter<String>? = null
    private var recyclerView2: RecyclerView? = null
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

        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val search = view.findViewById<EditText>(R.id.searchEditText)
        val spinner = view.findViewById<Spinner>(R.id.citySpinner)
        val explorerRecyclerView: RecyclerView = view.findViewById(R.id.explorerRecyclerView)

        var cities: MutableList<String>? = mutableListOf()
        val spainCities = listOf<String>(
            "Selecciona una ciudad",
            "Álava",
            "Albacete",
            "Alicante",
            "Almería",
            "Asturias",
            "Ávila",
            "Badajoz",
            "Barcelona",
            "Burgos",
            "Cáceres",
            "Cádiz",
            "Cantabria",
            "Castellón",
            "Ciudad Real",
            "Córdoba",
            "La Coruña",
            "Cuenca",
            "Gerona",
            "Granada",
            "Guadalajara",
            "Guipúzcoa",
            "Huelva",
            "Huesca",
            "Islas Baleares",
            "Jaén",
            "León",
            "Lérida",
            "Lugo",
            "Madrid",
            "Málaga",
            "Murcia",
            "Navarra",
            "Orense",
            "Palencia",
            "Las Palmas",
            "Pontevedra",
            "La Rioja",
            "Salamanca",
            "Segovia",
            "Sevilla",
            "Soria",
            "Tarragona",
            "Santa Cruz de Tenerife",
            "Teruel",
            "Toledo",
            "Valencia",
            "Valladolid",
            "Vizcaya",
            "Zamora",
            "Zaragoza"
        )
        cities!!.addAll(spainCities)

        recyclerView2 = view.findViewById(R.id.explorerRecyclerView)
        recyclerView2?.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 3, LinearLayout.VERTICAL, false)
        //gridLayoutManager.reverseLayout = true
        //gridLayoutManager.stackFromEnd = true
        recyclerView2?.layoutManager = gridLayoutManager
        mEvent = ArrayList()
        eventAdapter = context?.let {
            PostAdapter2(it, mEvent as ArrayList<Event>, true)
        }
        recyclerView2?.adapter = eventAdapter

        cityAdapter = this@SearchFragment.context?.let {
            ArrayAdapter(
                it,
                R.layout.support_simple_spinner_dropdown_item,
                cities
            )
        }
        spinner.adapter = cityAdapter

        var userVisited: User = User()
        var userVisitedUid: String = ""
        reference.child("users").child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        userVisitedUid = dataSnapshot.key.toString()
                        userVisited = User(
                            dataSnapshot.child("fullName").value.toString(),
                            dataSnapshot.child("username").value.toString(),
                            dataSnapshot.child("email").value.toString(),
                            dataSnapshot.child("birthDate").value.toString(),
                            dataSnapshot.child("password").value.toString(),
                            dataSnapshot.child("creationDate").value.toString(),
                            dataSnapshot.child("creationTime").value.toString()
                        )
                        userVisited.userUid = userVisitedUid
                        userVisited.imageUrl =
                            dataSnapshot.child("imageUrl").value.toString()
                        userVisited.city = dataSnapshot.child("city").value.toString()
                        spinner.setSelection(spainCities.indexOf(userVisited.city.toString()))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        "SearchFragment",
                        "Failed to read user data.",
                        error.toException()
                    )
                }
            })

        recyclerView = view.findViewById(R.id.searchRecyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        mUser = ArrayList()
        userAdapter = context?.let {
            UserAdapter(it, mUser as ArrayList<User>, true)
        }
        recyclerView?.adapter = userAdapter

        search.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search.text.toString() == "") {
                    mUser?.clear()
                    spinner.visibility = View.VISIBLE
                    explorerRecyclerView.visibility = View.VISIBLE
                } else {
                    explorerRecyclerView.visibility = View.INVISIBLE
                    spinner.visibility = View.INVISIBLE
                    recyclerView?.visibility = View.VISIBLE
                    reference.child("users").orderByChild("username")
                        .startAt(s.toString())
                        .endAt(s.toString() + "\uf8ff")
                        .limitToFirst(25)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                mUser?.clear()
                                for (snapshot in dataSnapshot.children) {
                                    var userVisitedUid: String = snapshot.key.toString()
                                    val user = User(
                                        snapshot.child("fullName").value.toString(),
                                        snapshot.child("username").value.toString(),
                                        snapshot.child("email").value.toString(),
                                        snapshot.child("birthDate").value.toString(),
                                        snapshot.child("password").value.toString(),
                                        snapshot.child("creationDate").value.toString(),
                                        snapshot.child("creationTime").value.toString()
                                    )
                                    user.userUid = userVisitedUid
                                    user.imageUrl = snapshot.child("imageUrl").value.toString()
                                    user.description =
                                        snapshot.child("description").value.toString()
                                    if (user != null && userVisitedUid != auth.currentUser!!.uid) {
                                        mUser?.add(user)
                                    }
                                }
                                userAdapter?.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w(
                                    "SearchFragment",
                                    "Failed to read user data.",
                                    error.toException()
                                )
                            }
                        })
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val city: String = spinner.adapter.getItem(position).toString()
                reference.child("users").child(auth.currentUser!!.uid).child("city")
                    .setValue(city)
                reference.child("posts").orderByChild("city")
                    .equalTo(city).limitToFirst(100)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            mEvent?.clear()
                            if (dataSnapshot.hasChildren()) {
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
                                        snapshot.child("capacityAvailable").value.toString()
                                            .toIntOrNull(),
                                        snapshot.child("accessPrice").value.toString()
                                            .toDoubleOrNull()
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
                                    }
                                }
                                eventAdapter?.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Log.w(
                                "SearchFragment",
                                "Failed to read posts data.",
                                error.toException()
                            )
                        }
                    })
            }

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
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}