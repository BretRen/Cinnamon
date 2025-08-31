package com.sosauce.cuteconnect.domain.model

import android.net.Uri

data class AboutMe(
    val name: String = "Me",
    val number: String = "+33 7 00 00 00 00",
    val photo: Uri? = null
)
