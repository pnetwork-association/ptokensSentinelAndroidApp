package com.ptokenssentinelandroidapp

import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class RustBridge(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private external fun callCore(input: String): String

  init {
    System.loadLibrary("ptokens_sentinel_core")
  }

  override fun getName() = "RustBridge"

  @ReactMethod
  fun callRustCore(b64Input: String, callback: Callback) {
    //var db: DatabaseWiring = DatabaseWiring()
    Log.d("[DEBUG] RustBridge", "`callRustCore` called with with str: $b64Input")
    callback.invoke(callCore(b64Input))
  }
}
