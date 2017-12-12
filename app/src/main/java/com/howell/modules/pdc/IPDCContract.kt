package com.howell.modules.pdc

import android.content.Context
import com.howell.modules.ImpBasePresenter
import com.howell.modules.ImpBaseView

/**
 * Created by Administrator on 2017/12/12.
 */
interface IPDCContract {
    interface IView : ImpBaseView{

    }
    interface IPresent : ImpBasePresenter{
        fun init(c:Context)
        fun queryDevice()
    }
}