package com.ptokenssentinelandroidapp

import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableNativeArray


class RustBridge(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private external fun callCore(input: ReadableArray): ByteArray

  init {
    System.loadLibrary("ptokens_sentinel_core")
  }

  override fun getName() = "RustBridge"

  @ReactMethod
  fun callRustCore(bytes: ReadableArray, callback: Callback) {
    Log.d("[DEBUG] RustBridge", "`callRustCore` called with with bytes: $bytes")

    val bytes = callCore(bytes)

    val retVal = WritableNativeArray();
    for (byte in bytes) {
      retVal.pushInt(byte.toInt())
    }
    callback.invoke(retVal)
  }
}
