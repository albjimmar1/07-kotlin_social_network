package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notification(
    var type: String? = "",
    var content: String? = "",
    var date: String? = "",
    var time: String? = "",
    var momentMillis: String? = ""
) {
    var eventId: String? = ""
    var imageEvent: String? = ""
    var userId: String? = ""
    var imageUser: String? = ""
}
