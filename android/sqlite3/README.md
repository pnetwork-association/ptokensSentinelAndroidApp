# Sqlite3 with sha3 extension enabled

The ufficial repo is here https://www.sqlite.org/src/tree

Steps performed:

1. Change the file `android_database_SQLiteConnection.cpp`, add into `nativeOpen()` function:

```C
err = sqlite3_enable_load_extension(db, 1);
if (err != SQLITE_OK) {
      throw_sqlite3_exception_errcode(env, err, "Could not enable the extension");
      return 0;
}
```

2. Create a folder `/ext` dove mettere i file `shathree.c` `sqlite3ext.`c e `Android.mk`, quest'ultimo con:
```makefile
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
ifeq ($(TARGET_ARCH), arm)
        LOCAL_CFLAGS += -DPACKED="__attribute__ ((packed))"
else
        LOCAL_CFLAGS += -DPACKED=""
endif
LOCAL_SRC_FILES += shathree.c
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/nativehelper/
LOCAL_MODULE:= libshathree
LOCAL_LDLIBS += -ldl -llog 
include $(BUILD_SHARED_LIBRARY)
```
3. Add `include $(LOCAL_PATH)/ext/Android.mk` to `Android.mk` of the project

4. Add `OCAL_CFLAGS += -DSQLITE_ENABLE_SHA3` to `Android.mk` di sqliteX

5. Add `APP_MODULES := libsqliteX libshathree` to `Application.mk`

6. Build with `gradlew assembleRelease`, this will produce the `sqlite3-release.aar`

7. Import the created package .aar in Android studio as described in [official documentation, step 2.](https://sqlite.org/android/doc/trunk/www/install.wiki)  
