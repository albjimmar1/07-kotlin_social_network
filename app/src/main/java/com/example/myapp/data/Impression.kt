package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Impression(
    var autor: String? = "",
    var event: String? = "",
    var date: String? = "",
    var time: String? = "",
    var momentMillis: String? = ""
)
