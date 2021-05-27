package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    var autor: String? = "",
    var receiver: String? = "",
    var content: String? = "",
    var date: String? = "",
    var time: String? = "",
    var momentMillis: String? = ""
)
