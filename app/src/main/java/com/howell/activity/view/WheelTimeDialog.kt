package com.howell.activity.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.howell.activity.PlayViewActivity
import com.howell.activity.fragment.WheelTimeFragment
import com.howell.modules.player.bean.CameraItemBean
import com.howell.pdcstation.R
import com.howellsdk.utils.Util

/**
 * Created by Administrator on 2017/12/6.
 */
class WheelTimeDialog() : DialogFragment() {

    lateinit var mBegWheel : WheelTimeFragment
    lateinit var mEndWheel : WheelTimeFragment
    lateinit var mTvBeg : TextView
    lateinit var mTvEnd : TextView
    lateinit var mCam: CameraItemBean

    @SuppressLint("ValidFragment")
    constructor(item:CameraItemBean):this(){
        mCam = item
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,R.style.wheel_dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(getString(R.string.wheel_dialog_title))
                .setNegativeButton(getString(R.string.cancel),null)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val begStr = Util.Date2String(mBegWheel.mWheel?.time)
                    val endStr = Util.Date2String(mEndWheel.mWheel?.time)
//                    Log.i("123","beg=$begStr  end=$endStr")
                    startActivity(Intent(context,PlayViewActivity::class.java)
                            .putExtra("CameraItem",mCam)
                            .putExtra("BegTime",begStr)
                            .putExtra("EndTime",endStr)
                            .putExtra("isPlayback",true))
                }.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater!!.inflate(R.layout.wheel_dialog_container,container,false)
        val vp = v.findViewById<ViewPager>(R.id.wheel_vp)
        val tab = v.findViewById<TabLayout>(R.id.wheel_tab)
        mTvBeg = v.findViewById(R.id.wheel_sel_beg)
        mTvEnd = v.findViewById(R.id.wheel_sel_end)
        tab.addTab(tab.newTab().setText(getString(R.string.wheel_dialog_beg_time)))
        tab.addTab(tab.newTab().setText(getString(R.string.wheel_dialog_end_time)))
//        tab.tabGravity = TabLayout.GRAVITY_FILL
        tab.tabMode = TabLayout.MODE_SCROLLABLE
        vp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))
        tab.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(t: TabLayout.Tab?) {
                vp.currentItem = t?.position?:0
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })
        vp.adapter = MyFragmentPagerAdapter(childFragmentManager,getViewList())
        (dialog as AlertDialog ).setView(v)
        return v
    }

    private fun getViewList():ArrayList<Fragment>{
        val l:ArrayList<Fragment> = ArrayList()
        mBegWheel = WheelTimeFragment().setChangedLinstener(object : WheelTimeFragment.OnTimeChangedLinstener{
            override fun onTimeChanged(s: String) {
                mTvBeg.text = getString(R.string.wheel_dialog_beg_time)+": $s"
            }
        })
        mEndWheel = WheelTimeFragment().setChangedLinstener(object :WheelTimeFragment.OnTimeChangedLinstener{
            override fun onTimeChanged(s: String) {
                mTvEnd.text = getString(R.string.wheel_dialog_end_time)+": $s"
            }
        })
        l.add(mBegWheel)
        l.add(mEndWheel)
        return l
    }

     inner class MyFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){
        private var mList:ArrayList<Fragment> = ArrayList()
        private var mTitle:ArrayList<String> = ArrayList()

         constructor(fm:FragmentManager,l:ArrayList<Fragment>):this(fm){
             mList = l
             mTitle.add(getString(R.string.wheel_dialog_beg_time))
             mTitle.add(getString(R.string.wheel_dialog_end_time))
         }

        override fun getItem(position: Int): Fragment = mList[position]

        override fun getCount(): Int = mList.size

        override fun getPageTitle(position: Int): CharSequence = mTitle[position]
    }

}