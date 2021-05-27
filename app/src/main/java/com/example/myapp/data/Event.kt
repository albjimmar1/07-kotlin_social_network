package com.example.myapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Event(
    var imageUrl: String? = "",
    var autor: String? = "",
    var title: String? = "",
    var description: String? = "",
    var category: String? = "",
    var city: String? = "",
    var location: String? = "",
    var creationDate: String? = "",
    var creationTime: String? = "",
    var startDate: String? = "",
    var endDate: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var capacity: Int? = 0,
    var capacityAvailable: Int? = 0,
    var accessPrice: Double? = 0.0,
) {
    var comments: MutableList<Comment>? = mutableListOf()
    var imgoingtos: MutableList<Intention>? = mutableListOf()
    var impressions: MutableList<Impression>? = mutableListOf()
    var imageAutor: String? = ""
    var eventUid: String = ""
}
