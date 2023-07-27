package com.ptokenssentinelandroidapp;

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext 
import com.facebook.react.uimanager.ViewManager 
import java.util.*

class RustBridgePackage (): ReactPackage {
   override fun createNativeModules (reactContext: ReactApplicationContext): List<NativeModule> {
      val modules = ArrayList<NativeModule>()
      modules.add(RustBridge(reactContext))
      return modules
   }

   override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
      return Collections.emptyList<ViewManager<*, *>>()
   }
}
