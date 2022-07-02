package ru.pl.draganddraw

import android.graphics.PointF
import android.os.Parcelable
import androidx.core.graphics.minus
import kotlinx.parcelize.Parcelize


@Parcelize
data class Box(
    val start: PointF, var end: PointF = start,
    var angle: Float = 0f
) : Parcelable {


    val left: Float
        get() = Math.min(start.x, end.x)

    val right: Float
        get() = Math.max(start.x, end.x)

    val top: Float
        get() = Math.min(start.y, end.y)

    val bottom: Float
        get() = Math.max(start.y, end.y)
}
