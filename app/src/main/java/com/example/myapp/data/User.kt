package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var fullName: String? = "",
    var username: String? = "",
    var email: String? = "",
    var birthDate: String? = "",
    var password: String? = "",
    var creationDate: String? = "",
    var creationTime: String? = ""
) {
    var visits: MutableList<Visit>? = mutableListOf()
    var followers: MutableList<Follow>? = mutableListOf()
    var following: MutableList<Follow>? = mutableListOf()
    var posts: MutableList<Event>? = mutableListOf()
    var chats: MutableList<MutableList<Message>> = mutableListOf()
    var description: String? = ""
    var city: String? = ""
    var imageUrl: String? = ""
    var userUid: String = ""
}
