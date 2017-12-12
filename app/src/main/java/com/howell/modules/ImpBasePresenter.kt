package com.howell.modules

/**
 * Created by Administrator on 2017/11/27.
 */
interface ImpBasePresenter {
    fun bindView(view: ImpBaseView)
    fun unbindView()
}