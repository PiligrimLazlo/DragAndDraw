package ru.pl.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.minus
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

private const val TAG = "BoxDrawingView"

class BoxDrawingView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentBox: Box? = null
    private var startSecondPointer: PointF? = null

    private val boxes = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000.toInt()
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val startFirstPointer = PointF(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentBox = Box(startFirstPointer).also {
                    boxes.add(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.findPointerIndex(1) != -1) {
                    val secondPointerEndIndex = event.findPointerIndex(1)
                    val endSecondPointer =
                        PointF(event.getX(secondPointerEndIndex), event.getY(secondPointerEndIndex))

                    val currentAngle = startSecondPointer?.let {
                        calculateAngleABC(
                            it,
                            startFirstPointer,
                            endSecondPointer
                        )
                    } ?: 0f
                    Log.i(TAG, "currentAngle is $currentAngle")
                    currentBox?.angle = currentAngle
                }

                updateCurrentBox(startFirstPointer, currentBox)
            }
            MotionEvent.ACTION_UP -> {
                updateCurrentBox(startFirstPointer, currentBox)
                nullFields()
            }
            MotionEvent.ACTION_CANCEL -> {
                nullFields()
            }


            MotionEvent.ACTION_POINTER_DOWN -> {
                val secondPointerStart = event.findPointerIndex(1)
                startSecondPointer =
                    PointF(event.getX(secondPointerStart), event.getY(secondPointerStart))
            }
            MotionEvent.ACTION_POINTER_UP -> {
                updateCurrentBox(startFirstPointer, currentBox)
                nullFields()
            }
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)

        boxes.forEach { box ->
            val path = Path()
            path.addRect(box.left, box.top, box.right, box.bottom, Path.Direction.CW)
            canvas.save()
            canvas.rotate(box.angle, 700f, 700f)
            canvas.drawPath(path, boxPaint)
            canvas.restore()
        }
    }

    private fun updateCurrentBox(current: PointF, currBox: Box?) {
        currBox?.let {
            it.end = current
            invalidate()
        }
    }

    private fun nullFields() {
        currentBox = null
        startSecondPointer = null
    }

    //calculate angle ABC
    private fun calculateAngleABC(A: PointF, B: PointF, C: PointF): Float {
        val BA = A - B
        val BC = C - B

        val BABC = (BA.x * BC.x) + (BA.y * BC.y)

        val BAlength = sqrt((BA.x * BA.x) + (BA.y * BA.y))
        val BClength = sqrt((BC.x * BC.x) + (BC.y * BC.y))

        val cosABC = BABC / (BAlength * BClength)

        val angle = ((180 / PI) * acos(cosABC)).toFloat()

        //where started draw
        val topLeft = B.x <= A.x && B.y <= A.y
        val topRight = B.x >= A.x && B.y <= A.y
        val botLeft = B.x <= A.x && B.y >= A.y
        val botRight = B.x >= A.x && B.y >= A.y


        if (topLeft && (C.y <= A.y || C.x >= A.x))
            return -angle
        else if (topRight && (C.y >= A.y || C.x >= A.x))
            return -angle
        else if (botLeft && (C.y <= A.y || C.x <= A.x))
            return -angle
        else if (botRight && (C.y >= A.y || C.x <= A.x))
            return -angle
        return angle

    }

    override fun onSaveInstanceState(): Parcelable {
        val resultBundle = Bundle()
        resultBundle.putInt("count", boxes.size)
        boxes.forEach { box ->
            resultBundle.putParcelable(boxes.indexOf(box).toString(), box)
        }
        resultBundle.putParcelable("superState", super.onSaveInstanceState())
        return resultBundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (state is Bundle) {
            viewState = state.getParcelable("superState")

            val count = state.get("count") as Int
            for (i in 0 until count) {
                val currentBoxFromBundle = state.getParcelable<Box>(i.toString())
                boxes.add(currentBoxFromBundle!!)
            }
        }

        super.onRestoreInstanceState(viewState)
    }


}