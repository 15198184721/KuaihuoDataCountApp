package com.data.count.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import com.kuaihuo.data.count.KuaihuoCountManager

class TestViewGroup : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("event","TestViewGroup->dispatchTouchEvent ev.acticon = ${ev!!.action}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("event","TestViewGroup->onInterceptTouchEvent ev.acticon = ${ev!!.action}")
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("event","TestViewGroup->onTouchEvent ev.acticon = ${ev!!.action}")
        return super.onTouchEvent(ev)
    }

}