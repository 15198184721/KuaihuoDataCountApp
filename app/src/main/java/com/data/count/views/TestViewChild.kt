package com.data.count.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.kuaihuo.data.count.KuaihuoCountManager

class TestViewChild : View {
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
        Log.e("event","TestViewChild->dispatchTouchEvent ev.acticon = ${ev!!.action}")
        return super.dispatchTouchEvent(ev)
    }
    
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.e("event","TestViewChild->onTouchEvent ev.acticon = ${event!!.action}")
        return super.onTouchEvent(event)
    }

}