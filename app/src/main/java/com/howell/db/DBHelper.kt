package com.howell.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by Administrator on 2017/12/18.
 */
class DBHelper(context :Context, name:String, factory: SQLiteDatabase.CursorFactory?, version: Int ): SQLiteOpenHelper(context,name,factory, version) {


    override fun onCreate(db: SQLiteDatabase?) {
        val sqlUserInfo = ("create table userinfo(id integer primary key autoincrement,"
                + "fpid integer,"
                + "username varchar(80),"
                + "userpassword varchar(20),"
                + "ip varchar(20),"
                + "port integer,"
                + "ssl integer,"
                + "station integer);")

        db?.execSQL(sqlUserInfo)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}