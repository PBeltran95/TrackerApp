package ar.com.example.distancetracker.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Result(
    var distance:String,
    var time:String
) : Parcelable
