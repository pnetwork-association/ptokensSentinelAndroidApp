extern crate android_logger;

use log::Level;
use jni::JNIEnv;
use jni::sys::jstring;
use jni::objects::JString;
use ptokens_core::Result;
use android_logger::Config;

pub fn extract_result_or_raise_exception(env: &JNIEnv, result: Result<String>) -> jstring {
    match result {
        Ok(r) => env.new_string(r ).unwrap().into_inner(),
        Err(e) => {
            let err_msg = format!("{}", e);
            if env.exception_check().unwrap() {
                env.exception_describe().unwrap();
                env.exception_clear().unwrap();
            }

            env.new_string(&err_msg).unwrap().into_inner()
        }
    }
}

// This way we can get an error if the unwrapping of the result
// fails, otherwise the panic gets swallowed by execution
pub fn maybe_extract_jstring(env: &JNIEnv, s: JString) -> Option<String> {
    match env.get_string(s) {
        Ok(v) => Some(v.into()),
        Err(e) => {
            error!("{}", e);
            None
        }
    }
}

pub fn setup_android_logger() {
    let config= Config::default().with_min_level(Level::Debug);
    android_logger::init_once(config);
    info!("✔ Android logger loaded");
}