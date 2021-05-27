package com.example.myapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.myapp.data.Event
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import java.text.DecimalFormat
import java.util.*


class AddPostActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocation: FusedLocationProviderClient
    private var lastMarker: Marker? = null

    private var cityAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.reference
        storage = FirebaseStorage.getInstance().reference

        val cancelEditBtn = findViewById<ImageView>(R.id.cancelImageView)
        val saveEditBtn = findViewById<ImageView>(R.id.saveImageView)

        val title = findViewById<EditText>(R.id.titleEditText)
        val description = findViewById<EditText>(R.id.descriptionEditText)
        val category = findViewById<EditText>(R.id.categoryEditText)
        val city = findViewById<Spinner>(R.id.citySpinner)
        val location = findViewById<EditText>(R.id.locationEditText)
        val startDate = findViewById<EditText>(R.id.startDateEditText)
        val endDate = findViewById<EditText>(R.id.endDateEditText)
        val startTime = findViewById<EditText>(R.id.startTimeEditText)
        val endTime = findViewById<EditText>(R.id.endTimeEditText)
        val capacity = findViewById<EditText>(R.id.capacityEditText)
        val capacityAvailable = findViewById<EditText>(R.id.capacityAvailableEditText)
        val accessPrice = findViewById<EditText>(R.id.accessPriceEditText)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

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

        cityAdapter = this?.let {
            ArrayAdapter(
                it,
                R.layout.support_simple_spinner_dropdown_item,
                cities
            )
        }
        city.adapter = cityAdapter

        startDate.setOnClickListener {
            val datePicker =
                DatePickerFragment2 { day, month, year -> startDate.setText("$day/" + (month + 1).toString() + "/$year") }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        endDate.setOnClickListener {
            val datePicker =
                DatePickerFragment2 { day, month, year -> endDate.setText("$day/" + (month + 1).toString() + "/$year") }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        startTime.setOnClickListener {
            val formatter = DecimalFormat("00")
            val timePicker =
                TimePickerFragment { hour, minute ->
                    startTime.setText(
                        "$hour:" + formatter.format(
                            minute
                        )
                    )
                }
            timePicker.show(supportFragmentManager, "timePicker")
        }

        endTime.setOnClickListener {
            val formatter = DecimalFormat("00")
            val timePicker =
                TimePickerFragment { hour, minute ->
                    endTime.setText(
                        "$hour:" + formatter.format(
                            minute
                        )
                    )
                }
            timePicker.show(supportFragmentManager, "timePicker")
        }

        CropImage.activity().setAspectRatio(1, 2).start(this)
        overridePendingTransition(R.anim.slide_down_to_center, R.anim.slide_center_to_up)

        cancelEditBtn.setOnClickListener {
            onBackPressed()
        }

        var citySelected: String = ""
        city.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                citySelected = city.adapter.getItem(position).toString()
            }
        }

        saveEditBtn.setOnClickListener {
            val fTitle: String = title.text.toString().trim()
            val fDescription: String = description.text.toString().trim()
            val fCategory: String = category.text.toString().trim()
            val fCity: String = citySelected
            val fLocation: String = location.text.toString().trim()
            //val formatDate = SimpleDateFormat("dd/MM/aaaa")
            //val fBirthDate: Date = formatDate.parse(birthDate.text.toString().trim())
            val fStartDate: String = startDate.text.toString().trim()
            val fEndDate: String = endDate.text.toString().trim()
            val fStartTime: String = startTime.text.toString().trim()
            val fEndTime: String = endTime.text.toString().trim()
            val fCapacity: Int? = capacity.text.toString().trim().toIntOrNull()
            val fCapacityAvailable: Int? = capacityAvailable.text.toString().trim().toIntOrNull()
            val fAccessPrice: Double? = accessPrice.text.toString().trim().toDoubleOrNull()
            var error: Boolean = false
            val regex = Regex(pattern = """\w+(\,*\s*(\w+)*\.*)*""")
            val regex2 = Regex(pattern = """\w+""")
            var post: Event = Event(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                0.0
            )
            if (imageUri != null) {
                if (!TextUtils.isEmpty(fTitle)) {
                    if (fTitle.contains("_")) {
                        title.error = "Title must not contain low bars."
                        error = true
                    } else if (!regex2.matches(fTitle)) {
                        title.error = "Title must only contain alphanumeric characters."
                        error = true
                    } else if (fTitle.length > 64) {
                        title.error = "Title must be 64 characters or less."
                        error = true
                    } else {
                        post.title = fTitle
                    }
                }
                if (!TextUtils.isEmpty(fDescription)) {
                    if (fDescription.contains("_")) {
                        description.error = "Description must not contain low bars."
                        error = true
                    } else if (!regex.matches(fDescription)) {
                        description.error = "Description must only contain alphanumeric characters."
                        error = true
                    } else if (fDescription.length > 128) {
                        description.error = "Description must be 128 characters or less."
                        error = true
                    } else {
                        post.description = fDescription
                    }
                }
                if (!TextUtils.isEmpty(fCategory)) {
                    if (fCategory.contains("_")) {
                        category.error = "Category must not contain low bars."
                        error = true
                    } else if (!regex2.matches(fCategory)) {
                        category.error = "Category must only contain alphanumeric characters."
                        error = true
                    } else if (fCategory.length > 32) {
                        category.error = "Category must be 32 characters or less."
                        error = true
                    } else {
                        post.category = fCategory
                    }
                }
                if (!TextUtils.isEmpty(fCity)) {
                    if (fCity == "Selecciona una ciudad" || fCity == null) {
                        (city.getSelectedView() as TextView).error = "City is required."
                        error = true
                    } else {
                        post.city = fCity
                    }
                }
                if (!TextUtils.isEmpty(fLocation)) {
                    post.location = fLocation
                } else {
                    location.error = "Location is required."
                    error = true
                }
                if (!TextUtils.isEmpty(fStartDate)) {
                    post.startDate = fStartDate
                } else {
                    startDate.error = "Start date is required."
                    error = true
                }
                if (!TextUtils.isEmpty(fEndDate)) {
                    post.endDate = fEndDate
                } else {
                    endDate.error = "End date is required."
                    error = true
                }
                if (!TextUtils.isEmpty(fStartTime)) {
                    post.startTime = fStartTime
                } else {
                    startTime.error = "Start time is required."
                    error = true
                }
                if (!TextUtils.isEmpty(fEndTime)) {
                    post.endTime = fEndTime
                } else {
                    endTime.error = "End time is required."
                    error = true
                }
                if (fCapacity != null) {
                    if (fCapacity < 0) {
                        capacity.error = "Capacity cannot be less than 0."
                        error = true
                    } else {
                        post.capacity = fCapacity
                    }
                }
                if (fCapacityAvailable != null) {
                    if (fCapacityAvailable < 0) {
                        capacityAvailable.error = "Capacity cannot be less than 0."
                        error = true
                    } else {
                        post.capacityAvailable = fCapacityAvailable
                    }
                }
                if (fAccessPrice != null) {
                    if (fAccessPrice < 0) {
                        accessPrice.error = "Access price cannot be less than 0."
                        error = true
                    } else {
                        post.accessPrice = fAccessPrice
                    }
                }
                if (error) {
                    return@setOnClickListener
                } else {
                    val postId: String =
                        database.getReference("posts").push().key.toString()
                    val fileRef =
                        storage.child("postImages").child(auth.currentUser!!.uid).child(postId)
                    var uploadTask = fileRef.putFile(imageUri!!)
                    uploadTask.continueWith { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        fileRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            fileRef.downloadUrl.addOnSuccessListener {
                                val imageUrl: String = it.toString()
                                val autor: String = auth.currentUser!!.uid
                                val c: Calendar = Calendar.getInstance()
                                val day: String = c.get(Calendar.DAY_OF_MONTH).toString()
                                val month: String = (c.get(Calendar.MONTH) + 1).toString()
                                val year: String = c.get(Calendar.YEAR).toString()
                                val hour: String = c.get(Calendar.HOUR_OF_DAY).toString()
                                val minute: String = c.get(Calendar.MINUTE).toString()
                                val second: String = c.get(Calendar.SECOND).toString()
                                val creationDate: String = "$day/$month/$year"
                                val creationTime: String = "$hour:$minute:$second"
                                post.imageUrl = imageUrl
                                post.autor = autor
                                post.creationDate = creationDate
                                post.creationTime = creationTime
                                reference.child("posts").child(postId).setValue(post)
                                reference.child("users").child(auth.currentUser!!.uid)
                                    .child("posts").child(postId)
                                    .setValue(post)
                            }.addOnFailureListener {

                            }
                            val downloadUri = task.result
                        } else {
                            // Handle failures
                        }
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(
                        R.anim.slide_up_to_center,
                        R.anim.slide_center_to_down
                    )
                }
            } else {
                Toast.makeText(
                    baseContext, "Failed to upload image.",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(
                    R.anim.slide_up_to_center,
                    R.anim.slide_center_to_down
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                imageUri = result.uri
                val postImageView = findViewById<ImageView>(R.id.poshImageView)
                postImageView.setImageURI(imageUri)
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
        //updateUI(currentUser)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        } else {
            mMap.isMyLocationEnabled = true
        }

        val location = findViewById<EditText>(R.id.locationEditText)

        val spain = LatLng(40.4166400, -3.7032700)
        //lastMarker = mMap.addMarker(MarkerOptions().position(spain).title("Marker in Spain"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(spain, 5.3f))

        //mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocation.lastLocation.addOnSuccessListener {
            if (it != null) {
                val location2 = LatLng(it.latitude, it.longitude)
                lastMarker = mMap.addMarker(
                    MarkerOptions().position(location2).title("Marker current location")
                )
                location.setText(lastMarker?.position?.latitude.toString() + "," + lastMarker?.position?.longitude.toString())
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location2, 12f))
            }
        }

        mMap.setOnMapLongClickListener {
            val markerOptions = MarkerOptions().position(it)
            /*markerOptions.icon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN
                )
            )*/
            if (lastMarker != null) {
                lastMarker!!.remove()
            }
            lastMarker = mMap.addMarker(markerOptions)
            location.setText(lastMarker?.position?.latitude.toString() + "," + lastMarker?.position?.longitude.toString())
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
        }
    }
}