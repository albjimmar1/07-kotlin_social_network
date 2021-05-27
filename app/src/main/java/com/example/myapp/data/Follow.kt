package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Follow(
    var autor: String? = "",
    var receiver: String? = "",
    var date: String? = "",
    var time: String? = "",
    var momentMillis: String? = ""
)
