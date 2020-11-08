package com.anzen.android.examenandroid.utils

import java.io.Serializable

data class EcobiciContentModel(
    var id: String? = null,
    val district: String? = null,
    val lon: String? = null,
    val lat: String? = null,
    val bikes: String? = null,
    val slots: String? = null,
    val zip: String? = null,
    val address: String? = null,
    val addressNumber: String? = null,
    val nearbyStations: String? = null,
    val status: String? = null,
    val name: String? = null,
    val stationType: String? = null
): Serializable