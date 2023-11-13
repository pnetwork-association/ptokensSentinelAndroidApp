# Sqlite3 with sha3 extension enabled

The aim is to customize the SQLite dependency in order to add the `sha3` (a.k.a. `shathree`) extension.

## Steps

1. Download the android bindings for SQLite
```sh
fossil clone http://www.sqlite.org/android android.fossil
mkdir sqlite-android && cd sqlite-android
fossil open ../android.fossil
```

2. Download the official SQLite repo
```sh
fossil clone http://www.sqlite.org sqlite.fossil
mkdir sqlite && cd sqlite
fossil open ../sqlite.fossil
fossil update release
```

3. Apply the patch (Check the [Appendix](#appendix) to understand what and why)
```sh
fossil patch < sqlite-sha3.aar.patch
```

4. Copy the following files into the `sqlite` folder:

```sh
cp /path/to/sqlite/ext/misc/shathree.c /path/to/sqlite-android/sqlite3/src/main/jni/sqlite/
cp /path/to/sqlite/ext/sqlite3ext.h /path/to/sqlite-android/sqlite3/src/main/jni/sqlite/
```

5. Build the .aar package (you may need to set the `ndkVersion` accordingly to one you have installed)
```sh
./gradlew clean assembleRelease
```

6. Copy the generated `.aar` package into the sqlite3 module of this project
```sh
cp /path/to/sqlite/sqlite3/build/outputs/aar/sqlite3-release.aar .
```


### Vital resources

 - [The ufficial repo](https://www.sqlite.org/src/tree)
 - [SQLite extensions loading](https://www.sqlite.org/loadext.html)
 - [SQLite Android bindings](https://sqlite.org/android/doc/trunk/www/install.wiki)
 - [SQLite Documentation](https://sqlite.org/docs.html)

## Appendix


1. We specify that we want two shared libraries imported into the `.aar` file
  - **libsqliteX.so:** the whole SQLite framework
  - **libshathree.so:** the extension
Both should end up inside each architecture of the aar file (you can check by unzip-ing it)
```diff
--- sqlite3/src/main/jni/Application.mk
+++ sqlite3/src/main/jni/Application.mk
@@ -1,1 +1,2 @@
 APP_STL:=c++_static
+APP_MODULES := libsqliteX libshathree
```

2. We enable extension loading, we load the extension (by referecing it's lib name) and disable the extension loading for security reasons (loading the extension by the relative SQL command is not advised since it can be exploited for SQL injection attacks):

```diff
Index: sqlite3/src/main/jni/sqlite/android_database_SQLiteConnection.cpp
==================================================================
--- sqlite3/src/main/jni/sqlite/android_database_SQLiteConnection.cpp
+++ sqlite3/src/main/jni/sqlite/android_database_SQLiteConnection.cpp
@@ -182,10 +182,34 @@
     if (err != SQLITE_OK) {
         throw_sqlite3_exception(env, db, "Could not set busy timeout");
         sqlite3_close(db);
         return 0;
     }
+
+    // Add this in order to add shathree later
+    err = sqlite3_enable_load_extension(db, 1);
+    if (err != SQLITE_OK) {
+        throw_sqlite3_exception_errcode(env, err, "Could not enable the extension");
+        sqlite3_close(db);
+        return 0;
+    }
+
+    // Adding shathree extension
+    err = sqlite3_load_extension(db, "libshathree.so", 0, 0);
+    if (err != SQLITE_OK) {
+        throw_sqlite3_exception_errcode(env, err, "Could not load 'shathree' extension");
+        sqlite3_close(db);
+        return 0;
+    }
+
+    // Disable extension loading for security reasons
+    err = sqlite3_enable_load_extension(db, 0);
+    if (err != SQLITE_OK) {
+        throw_sqlite3_exception_errcode(env, err, "Could not disable the extension");
+        sqlite3_close(db);
+        return 0;
+    }
```

3. We change the makefile in order to build the `libshathree` library along with the `libsqliteX` one:
```diff
Index: sqlite3/src/main/jni/sqlite/Android.mk
==================================================================
--- sqlite3/src/main/jni/sqlite/Android.mk
+++ sqlite3/src/main/jni/sqlite/Android.mk
@@ -1,8 +1,13 @@

 LOCAL_PATH:= $(call my-dir)
 include $(CLEAR_VARS)
+
+LOCAL_MODULE := shathree
+LOCAL_SRC_FILES := shathree.c
+include $(BUILD_SHARED_LIBRARY)
+include $(CLEAR_VARS)

 # If using SEE, uncomment the following:
 # LOCAL_CFLAGS += -DSQLITE_HAS_CODEC

 #Define HAVE_USLEEP, otherwise ALL sleep() calls take at least 1000ms
```
