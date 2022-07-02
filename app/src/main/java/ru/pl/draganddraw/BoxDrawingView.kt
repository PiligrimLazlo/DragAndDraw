package ru.pl.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

private const val TAG = "BoxDrawingView"

class BoxDrawingView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentBox: Box? = null
    private var currentBox2: Box? = null

    private val boxes = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000.toInt()
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        var current2 = PointF()
        if (event.findPointerIndex(1) != -1) {
            val pointerIndex = event.findPointerIndex(1)
            current2 = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
        }

        fun handleActionUp() {
            if (event.actionIndex == event.findPointerIndex(0)) {
                updateCurrentBox(current, currentBox)
                currentBox = null
            } else if (event.actionIndex == event.findPointerIndex(1)) {
                updateCurrentBox(current2, currentBox2)
                currentBox2 = null
            }
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentBox = Box(current).also {
                    boxes.add(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.findPointerIndex(0) != -1)
                    updateCurrentBox(current, currentBox)
                if (event.findPointerIndex(1) != -1)
                    updateCurrentBox(current2, currentBox2)


                Log.i(
                    TAG, "action index ${event.actionIndex}, pointerIndex of id0:" +
                            "${event.findPointerIndex(0)}, pointerIndex of id1: ${
                                event.findPointerIndex(1)
                            }"
                )


            }
            MotionEvent.ACTION_UP -> {
                handleActionUp()
            }
            MotionEvent.ACTION_CANCEL -> {
                currentBox = null
                currentBox2 = null
            }


            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.actionIndex == event.findPointerIndex(1)) {
                    currentBox2 = Box(current2).also {
                        boxes.add(it)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                handleActionUp()
            }
        }

        /*Log.i(TAG, "$action2 at x=${secondCurrent.x}, y=${secondCurrent.y}")*/
        /*Log.i(TAG, "$action at x=${current.x}, y=${current.y}")*/
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)

        boxes.forEach { box ->
            canvas.drawRect(box.left, box.top, box.right, box.bottom, boxPaint)
        }
    }

    private fun updateCurrentBox(current: PointF, currBox: Box?) {
        currBox?.let {
            it.end = current
            invalidate()
        }
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