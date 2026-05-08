package com.partyhub.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    val id: String,
    val name: String
) : Parcelable
