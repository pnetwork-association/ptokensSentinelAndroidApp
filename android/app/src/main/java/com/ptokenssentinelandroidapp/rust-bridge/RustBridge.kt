package com.ptokenssentinelandroidapp

import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.ptokenssentinelandroidapp.database.DatabaseWiring
import com.ptokenssentinelandroidapp.database.SQLiteHelper
import com.ptokenssentinelandroidapp.strongbox.Strongbox


class RustBridge(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private external fun callCore(strongbox: Strongbox, db: DatabaseWiring, input: String): String

  val context = this.reactApplicationContext.applicationContext

  val CLASS_NAME: String = "RustBridge"

  var db: DatabaseWiring? = null

  var strongbox: Strongbox? = null

  init {
    System.loadLibrary("sentinel_strongbox")
    this.strongbox = Strongbox(this.context)
  }

  override fun getName() = "RustBridge"

  @ReactMethod
  fun openDb(successCallback: Callback, failureCallback: Callback) {
    Log.d("[INFO]" + this.CLASS_NAME, "opening db...")
    if (this.db != null) {
      Log.d("[DEBUG]" + this.CLASS_NAME, "db already opened")
      successCallback.invoke()
      return
    } 

    try {
      this.db = DatabaseWiring(this.context, SQLiteHelper(context).writableDatabase, false)
      Log.d("[INFO]" + this.CLASS_NAME, "opened SQL database")
      successCallback.invoke()
      return
    } catch (e: Exception) {
      Log.d("[ERROR]" + this.CLASS_NAME, "error opening SQL database $e")
      failureCallback.invoke(e.message)
      return
    }
  }

  @ReactMethod
  fun closeDb(successCallback: Callback, failureCallback: Callback) {
    Log.d("[INFO]" + this.CLASS_NAME, "closing db...")
    if (this.db == null) {
      Log.d("[DEBUG]" + this.CLASS_NAME, "db already closed")
      successCallback.invoke()
      return
    } 

    try {
      SQLiteHelper(context).close()
      this.db = null
      Log.d("[DEBUG]" + this.CLASS_NAME, "closed SQL database")
      successCallback.invoke()
      return
    } catch (e: Exception) {
      Log.d("[ERROR]" + this.CLASS_NAME, "error closing SQL database $e")
      failureCallback.invoke(e.message)
      return
    }
  }

  @ReactMethod
  fun callRustCore(b64Input: String, successCallback: Callback, failureCallback: Callback) {
    try {
      Log.d("[INFO]" + this.CLASS_NAME, "`callRustCore` successfully")
      successCallback.invoke(callCore(this.strongbox!!, this.db!!, b64Input))
      return
    } catch(e: Exception) {
      Log.d("[ERROR]" + this.CLASS_NAME, "failed to call rust core with exception: $e")
      failureCallback.invoke(e.message)
      return
    }
  }

  @ReactMethod
  fun dropDb(successCallback: Callback, failureCallback: Callback) {
    try {
      this.db!!.drop()
      Log.d("[INFO]" + this.CLASS_NAME, "database dropped!")
      successCallback.invoke()
    } catch(e: Exception) {
      Log.d("[ERROR]" + this.CLASS_NAME, "could not drop database, it was null")
      failureCallback.invoke(e.message)
    }
  }
}
