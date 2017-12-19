package com.howell.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.howell.bean.UserDbBean
import java.util.ArrayList

/**
 * Created by Administrator on 2017/12/18.
 */
class UserDao {
    var dbHelper: DBHelper? = null


    constructor(c: Context ,name: String ,version: Int ){
        dbHelper = DBHelper(c,name,null,version)
    }

    fun insert(info: UserDbBean) {
        val db = dbHelper?.writableDatabase
        val sql = "insert into userinfo (fpid,username,userpassword,ip,port,ssl,station)values(?,?,?,?,?,?,?);"
        db?.execSQL(sql, arrayOf<Any?>(info.fpId, info.name, info.password, info.ip, info.port, info.ssl,info.station))
        db?.close()
    }


    fun updataByFpID(info: UserDbBean)  {
        val db = dbHelper?.writableDatabase
        val sql = "update userinfo set fpid=?,username=?,userpassword=?,ip=?,port=?,ssl=?,station=? where fpid=?;"
        db?.execSQL(sql, arrayOf<Any?>(info.fpId, info.name, info.password, info.ip, info.port,info.ssl,info.station,info.fpId))
        db?.close()
    }

    fun updataByName(info: UserDbBean) {
        val db = dbHelper?.writableDatabase
        val sql = "update userinfo set fpid=?,username=?,userpassword=?,ip=?,port=?,ssl=?,station=? where username=?;"
        db?.execSQL(sql, arrayOf<Any?>(info.fpId, info.name, info.password, info.ip, info.port, info.ssl,info.station, info.name))
        db?.close()
    }

    fun deleteByFpID(fpId: Int) {
        val db = dbHelper?.writableDatabase
        val sql = "delete from userinfo where fpid=?;"
        db?.execSQL(sql, arrayOf<Any>(fpId))
        db?.close()
    }

    fun deleteAll() {
        val db = dbHelper?.writableDatabase
        val sql = "delete  from userinfo;"
        db?.execSQL(sql, arrayOf())
        db?.close()
    }

    fun queryAll(): List<UserDbBean> {
        val data = ArrayList<UserDbBean>()
        val db = dbHelper?.writableDatabase
        val sql = "select * from userinfo order by id asc"
        val cursor = db?.rawQuery(sql, null)
        while (cursor!!.moveToNext()) {
            val fp = cursor.getInt(cursor.getColumnIndex("fpid"))
            val userName = cursor.getString(cursor.getColumnIndex("username"))
            val userPassword = cursor.getString(cursor.getColumnIndex("userpassword"))
            val ip = cursor.getString(cursor.getColumnIndex("ip"))
            val port = cursor.getInt(cursor.getColumnIndex("port"))
            val ssl = cursor.getInt(cursor.getColumnIndex("ssl"))
            val station = cursor.getInt(cursor.getColumnIndex("station"))
            data.add(UserDbBean(fp,userName,userPassword,ip,port,ssl,station))
        }
        cursor.close()
        db?.close()
        return data
    }

    fun findByfpId(fpID: Int): Boolean {
        val db = dbHelper?.writableDatabase
        val sql = "select * from userinfo where fpid=?;"
        val cursor = db?.rawQuery(sql, arrayOf(fpID.toString() + ""))
        val res = cursor?.moveToNext()==true
        cursor?.close()
        db?.close()
        return res
    }

    fun findByName(userName: String): Boolean {
        val db = dbHelper?.writableDatabase
        val sql = "select * from userinfo where username=?;"
        val cursor = db?.rawQuery(sql, arrayOf(userName + ""))
        val res = cursor?.moveToNext()==true
        cursor?.close()
        db?.close()
        return res
    }

    fun findByNameAndIP(userName: String, ip: String): Boolean {
        val db = dbHelper?.writableDatabase
        val sql = "select * from userinfo where username=? and ip=?;"
        val cursor = db?.rawQuery(sql, arrayOf(userName + "", ip + ""))
        val res = cursor?.moveToNext()==true
        cursor?.close()
        db?.close()
        return res
    }

    fun queryByfpID(fpId: Int): List<UserDbBean> {
        val db = dbHelper?.writableDatabase
        val data = ArrayList<UserDbBean>()
        val sql = "select * from userinfo where fpid=?;"
        val cursor = db?.rawQuery(sql, arrayOf(fpId.toString() + ""))
        while (cursor!!.moveToNext()) {
            val fpId = cursor.getInt(cursor.getColumnIndex("fpid"))
            val userName = cursor.getString(cursor.getColumnIndex("username"))
            val userPassword = cursor.getString(cursor.getColumnIndex("userpassword"))
            val ip = cursor.getString(cursor.getColumnIndex("ip"))
            val port = cursor.getInt(cursor.getColumnIndex("port"))
            val ssl = cursor.getInt(cursor.getColumnIndex("ssl"))
            val station = cursor.getInt(cursor.getColumnIndex("station"))
            data.add(UserDbBean(fpId,userName,userPassword,ip,port,ssl,station))
        }
        cursor.close()
        db?.close()
        return data
    }

    fun queryByName(userName: String): List<UserDbBean> {
        val db = dbHelper?.writableDatabase
        val data = ArrayList<UserDbBean>()
        val sql = "select * from userinfo where username=?;"
        val cursor = db?.rawQuery(sql, arrayOf(userName + ""))
        while (cursor!!.moveToNext()) {
            val fpId = cursor.getInt(cursor.getColumnIndex("fpid"))
            val name = cursor.getString(cursor.getColumnIndex("username"))
            val pwd = cursor.getString(cursor.getColumnIndex("userpassword"))
            val ip = cursor.getString(cursor.getColumnIndex("ip"))
            val port = cursor.getInt(cursor.getColumnIndex("port"))
            val ssl = cursor.getInt(cursor.getColumnIndex("ssl"))
            val station = cursor.getInt(cursor.getColumnIndex("station"))
            data.add(UserDbBean(fpId,name,pwd,ip,port,ssl,station))
        }
        cursor.close()
        db?.close()
        return data
    }

    fun close() {
        dbHelper?.close()
        dbHelper = null
    }

}