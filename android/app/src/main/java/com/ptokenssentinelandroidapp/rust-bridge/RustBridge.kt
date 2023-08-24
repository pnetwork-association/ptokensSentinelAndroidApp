package com.ptokenssentinelandroidapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.ptokenssentinelandroidapp.database.DatabaseWiring
import com.ptokenssentinelandroidapp.database.SQLiteHelper


class RustBridge(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private external fun callCore(db: DatabaseWiring, input: String): String
  val context = this.reactApplicationContext.applicationContext
  val TAG: String = "RustBridge"
  val db: DatabaseWiring = DatabaseWiring(
    this.context,
    this.getSqlDatabase(this.context),
    false
  )

  init {
    System.loadLibrary("ptokens_sentinel_core")
  }

  override fun getName() = "RustBridge"

  fun getSqlDatabase(context: Context): SQLiteDatabase? {
    val helper = SQLiteHelper(context)
    var db: SQLiteDatabase? = null
    //if (flagWriteableDatabase) {
      db = helper.writableDatabase
    //} else if (flagReadableDatabase) {
    //  db = helper.readableDatabase
    //}
    Log.d("[DEBUG]" + this.TAG, "got SQL database")
    return db
  }

  fun getDbWiring(): DatabaseWiring {
    return db
  }

  @ReactMethod
  fun callRustCore(b64Input: String, callback: Callback) {
    var context = this.reactApplicationContext.applicationContext;
    //var db: DatabaseWiring = DatabaseWiring(context, this.getSqlDatabase(context), false)
    Log.d("[DEBUG]" + this.TAG, "`callRustCore` called with with str: $b64Input")
    callback.invoke(callCore(this.db, b64Input))
  }
}