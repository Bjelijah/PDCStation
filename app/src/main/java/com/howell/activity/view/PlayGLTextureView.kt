package com.howell.activity.view

import android.content.Context
import android.util.AttributeSet

import com.howellsdk.api.player.GLESRendererImpl
import com.howellsdk.api.player.GLESTextureView

/**
 * Created by Administrator on 2017/6/23.
 */


class PlayGLTextureView(private val mContext: Context, attrs: AttributeSet) : ZoomableTextureView(mContext, attrs) {
    var mRenderer: GLESRendererImpl
    init {
        mRenderer = GLESRendererImpl(mContext, this, null)
        setRenderer(mRenderer)
        setRenderMode(GLESTextureView.RENDERMODE_WHEN_DIRTY)
    }
}
