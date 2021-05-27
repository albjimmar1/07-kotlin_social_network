package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Chat(
    var autor: String? = "",
    var receiver: String? = "",
    var lastMessage: String? = "",
    var date: String? = "",
    var time: String? = "",
    var momentMillis: String? = ""
)
