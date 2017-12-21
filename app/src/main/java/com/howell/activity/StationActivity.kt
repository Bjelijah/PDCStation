package com.howell.activity

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.support.v7.widget.AppCompatSpinner
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemSelected
import com.bumptech.glide.Glide
import com.howell.pdcstation.R

/**
 * Created by Administrator on 2017/12/19.
 */
class StationActivity :BaseActivity() {


    @BindView(R.id.station_iv)     lateinit var mView:ImageView
    @BindView(R.id.station_btn)    lateinit var mBtn:Button
    @BindView(R.id.station_sp)     lateinit var mSp:AppCompatSpinner

    var mAdapter:MyStationAdapter?=null

    override fun findView() {
        setContentView(R.layout.activity_station)
        ButterKnife.bind(this)
    }

    override fun initView() {
        Glide.with(this).load("https://unsplash.it/600/300/?random").centerCrop().into(mView)
        mAdapter = MyStationAdapter(this)
        mSp.adapter = mAdapter

    }

    override fun initData() {
        //getmSp
        test()
    }

    @OnClick(R.id.station_btn) fun onEntryClick(v: View){
        val intent = Intent(this,HomeActivity::class.java)
                .putExtra("account","howell")
                .putExtra("email","howell")
        startActivity(intent)
//        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, mView, "myImage").toBundle())
        finish()
//        mView.postDelayed({finish()},1000)
    }

    @OnItemSelected(R.id.station_sp) fun onSpSelect(pos:Int){
        Log.i("123","pos=$pos  ")
    }



    fun test(){
        val l = ArrayList<String>()
        l.add("test 1")
        l.add("test 2")
        l.add("test 3")
        l.add("test 4")
        l.add("test 5")
        (6..15).mapTo(l) { "test $it" }
        mAdapter?.setData(l)
    }

    inner class MyStationAdapter(var c:Context) : BaseAdapter(),SpinnerAdapter{
        var mList:ArrayList<String>?=null

        fun setData(l:ArrayList<String>){
            mList = l
            notifyDataSetChanged()
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = LinearLayout.inflate(c,R.layout.item_station,null)
            v.findViewById<TextView>(R.id.station_item).text = mList!![position]
            return v
        }

        override fun getItem(position: Int): Any = mList!![position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = mList?.size?:0


    }


}