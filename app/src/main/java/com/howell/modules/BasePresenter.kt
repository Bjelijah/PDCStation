package com.howell.modules

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Administrator on 2017/11/27.
 */
open class BasePresenter {
    private var mCompositeDisposable: CompositeDisposable?=null
    fun addDisposable(subscription:Disposable){
        if (mCompositeDisposable?.isDisposed()?:true){
            mCompositeDisposable= CompositeDisposable()
        }
        mCompositeDisposable?.add(subscription)
    }
    fun dispose() = mCompositeDisposable?.dispose()
}