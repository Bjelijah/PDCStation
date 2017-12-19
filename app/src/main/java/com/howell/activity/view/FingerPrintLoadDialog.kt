package com.howell.activity.view

/**
 * Created by Administrator on 2017/12/18.
 */
class FingerPrintLoadDialog : FingerPrintBaseDialog() {


    override fun onAuthenticated(id: Int) {
        super.onAuthenticated(id)
        signIn(id)
    }

    private fun signIn(id:Int){
        //Load info from db

    }

}