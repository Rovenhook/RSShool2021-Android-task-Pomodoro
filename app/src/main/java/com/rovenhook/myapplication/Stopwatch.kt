package com.rovenhook.myapplication

import android.os.Parcel
import android.os.Parcelable

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    val maxTimeMs: Long,
    var isStarted: Boolean,
    var startTime: Long = 0,
    var isFinished: Boolean
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(currentMs)
        parcel.writeLong(maxTimeMs)
        parcel.writeByte(if (isStarted) 1 else 0)
        parcel.writeLong(startTime)
        parcel.writeByte(if (isFinished) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Stopwatch> {
        override fun createFromParcel(parcel: Parcel): Stopwatch {
            return Stopwatch(parcel)
        }

        override fun newArray(size: Int): Array<Stopwatch?> {
            return arrayOfNulls(size)
        }
    }
}