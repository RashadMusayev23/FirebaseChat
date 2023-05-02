package com.mazeppa.firebasechat.model

import java.io.Serializable

/**
 * Rashad Musayev on 5/1/2023 - 11:45
 */

data class User(
    var uid: String? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var profileImage: String? = null,
) : Serializable
