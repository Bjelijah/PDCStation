package com.howell.activity


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.howell.activity.fragment.DeviceFragment
import com.howell.activity.fragment.HomeBaseFragment
import com.howell.activity.fragment.PDCFragment
import com.howell.pdcstation.R
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.octicons_typeface_library.Octicons
import java.util.*

/**
 * Created by Administrator on 2017/11/29.
 */
class HomeActivity:BaseActivity() {

    @BindView(R.id.toolbar)                 lateinit var mToolbar: Toolbar
    @BindView(R.id.collapsing_toolbar)      lateinit var mCollapsingTbLayout:CollapsingToolbarLayout
    @BindView(R.id.main_content)            lateinit var mRootView: View
    @BindView(R.id.floating_action_button)  lateinit var mAddBtn:FloatingActionButton
    @BindView(R.id.backdrop)                lateinit var mImageView:ImageView
    @BindView(R.id.viewpager)               lateinit var mViewPage:ViewPager

    private val ID_DRAWER_HOME:   Long  = 0x01
    private val ID_DRAWER_CENTER: Long  = 0x02
    private val ID_DRAWER_EXIT:   Long  = 0xe0
    private val ID_DRAWER_SERVER: Long  = 0x10
    private val ID_DRAWER_BIND:   Long  = 0x110
    private val ID_DRAWER_HELP:   Long  = 0x12
    private val ID_DRAWER_PUSH:   Long  = 0x13
    private val mUserIcon = intArrayOf(R.drawable.profile2, R.drawable.profile3, R.drawable.profile4, R.drawable.profile5, R.drawable.profile6)
    private lateinit var mHeaderResult:AccountHeader
    private lateinit var mDrawerResult:Drawer
    private lateinit var mFragments:ArrayList<HomeBaseFragment>
    private var mSavedInstanceState:Bundle?=null


    private val onDrawerListener = DrawerListener()
    private val onDrawerItemClickListener = DrawerItemClickListener()

    private val randomUserIcon:Drawable?
        get() {
            val id = (Math.random() * 5).toInt()
            return getDrawable(mUserIcon[id])
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        mSavedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        var s = mDrawerResult.saveInstanceState(outState)
        s = mHeaderResult.saveInstanceState(s)
        super.onSaveInstanceState(s, outPersistentState)
    }


    override fun findView() {
        setContentView(R.layout.activity_home)
        ButterKnife.bind(this@HomeActivity)
    }

    override fun initView() {
        mToolbar.showOverflowMenu()
        mToolbar.inflateMenu(R.menu.center_setting_action_menu)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        mCollapsingTbLayout.title=getString(R.string.app_name)
        buildHead(false,mSavedInstanceState)
        buildDrawer(mSavedInstanceState)
        initFragment()
    }

    override fun initData() {
//        mAddBtn.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera_add).actionBar().color(Color.WHITE))

        mAddBtn.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera_party_mode).actionBar().color(Color.WHITE))
//        mAddBtn.setImageDrawable(IconicsDrawable(this, GoogleMaterial.Icon.gmd_pause).actionBar().color(Color.WHITE))
        Glide.with(this).load("https://unsplash.it/600/300/?random").centerCrop().into(mImageView)
    }


    private fun getProfile():List<IProfile<*>> {
        val list = ArrayList<IProfile<*>>()
        val userName = intent.getStringExtra("account")
        val userMail = intent.getStringExtra("email")
        val mine     = ProfileDrawerItem().withName(userName).withEmail(userMail).withIcon(randomUserIcon)
        list.add(mine)
        return list
    }

    private fun buildHead(compact:Boolean,savedInstanceState:Bundle?){
        mHeaderResult = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.home_bk_dark)
                .withCompactStyle(compact)
                .addProfiles(getProfile())
                .withSavedInstance(savedInstanceState)
                .withOnAccountHeaderProfileImageListener(object :AccountHeader.OnAccountHeaderProfileImageListener{
                    override fun onProfileImageClick(view: View?, profile: IProfile<*>?, current: Boolean): Boolean {
                        return true
                    }

                    override fun onProfileImageLongClick(view: View?, profile: IProfile<*>?, current: Boolean): Boolean {
                        return true
                    }
                })
                .withOnlyMainProfileImageVisible(true)
                .withOnAccountHeaderListener(AccountHeader.OnAccountHeaderListener { _, _, current ->
                    if (current) return@OnAccountHeaderListener false
                    false
                })
                .withCloseDrawerOnProfileListClick(false)
                .build()
    }

    private fun buildDrawer(savedInstanceState:Bundle?){
        mDrawerResult = DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(mHeaderResult)
                .withToolbar(mToolbar)
                .withFullscreen(true)
                .addDrawerItems(
                        PrimaryDrawerItem().withName(R.string.home_drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withIdentifier(ID_DRAWER_HOME),
                        PrimaryDrawerItem().withName(R.string.home_drawer_item_center).withIcon(FontAwesome.Icon.faw_cloud).withIdentifier(ID_DRAWER_CENTER),
                        SectionDrawerItem().withName(R.string.home_drawer_second_head),
                        SecondaryDrawerItem().withName(R.string.home_drawer_server_address).withIcon(Octicons.Icon.oct_server).withIdentifier(ID_DRAWER_SERVER),
                        SecondaryDrawerItem().withName(R.string.home_drawer_push_service).withIcon(Octicons.Icon.oct_alert).withIdentifier(ID_DRAWER_PUSH),
                        SecondaryDrawerItem().withName(R.string.home_drawer_server_bind).withIcon(GoogleMaterial.Icon.gmd_8tracks).withIdentifier(ID_DRAWER_BIND),
                        SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_question).withEnabled(false).withIdentifier(ID_DRAWER_HELP)
                )
                .addStickyDrawerItems(
                        SecondaryDrawerItem().withName(R.string.home_drawer_logout).withIcon(FontAwesome.Icon.faw_outdent).withIdentifier(ID_DRAWER_EXIT)
                )
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .withOnDrawerListener(onDrawerListener)
                .withOnDrawerItemClickListener(onDrawerItemClickListener)
                .build()
    }

    private fun initFragment(){

        mFragments = ArrayList()
        mFragments.add(DeviceFragment())
        mFragments.add(PDCFragment())
        mViewPage.offscreenPageLimit = 3
        mViewPage.adapter = MyFragmentPagerAdatper(supportFragmentManager,mFragments)
        mViewPage.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }


    private fun funExit(){
        mRootView.postDelayed({startActivity(Intent(this@HomeActivity,LoginActivity::class.java))
            finish()},300)
    }

    private fun funHome(){}

    private fun funCenter(){}

    private fun funServer(){
        startActivity(Intent(this,ServerSetActivity::class.java))
    }

    private fun funBind(){}

    private fun funPush(){}

    inner class DrawerListener:Drawer.OnDrawerListener{
        override fun onDrawerOpened(drawerView: View?) {

        }

        override fun onDrawerClosed(drawerView: View?) {
            //todo save info
        }

        override fun onDrawerSlide(drawerView: View?, slideOffset: Float) {

        }

    }

    inner class DrawerItemClickListener:Drawer.OnDrawerItemClickListener{
        override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*, *>?): Boolean {
            when (drawerItem?.identifier){
                ID_DRAWER_EXIT    ->funExit()
                ID_DRAWER_HOME    ->funHome()
                ID_DRAWER_CENTER  ->funCenter()
                ID_DRAWER_SERVER  ->funServer()
                ID_DRAWER_BIND    ->funBind()
                ID_DRAWER_PUSH    ->funPush()
            }
            return false
        }
    }

    inner class MyFragmentPagerAdatper(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        lateinit var mList:List<HomeBaseFragment>
        lateinit var mTitle:ArrayList<String>
        constructor(fm:FragmentManager?,list: List<HomeBaseFragment>) : this(fm) {
            mList  = list
            mTitle = ArrayList()
            mTitle.add(getString(R.string.home_fragment_devices))
            mTitle.add(getString(R.string.home_fragment_pdc))

        }
        override fun getItem(position: Int): Fragment = mList[position]

        override fun getCount(): Int = mList.size

        override fun getPageTitle(position: Int): CharSequence = mTitle[position]
    }

}