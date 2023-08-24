#![cfg(target_os = "android")]

#[macro_use]
extern crate log;

mod base64;
mod database;
mod error;
mod traits;
mod type_aliases;

use self::{
    base64::{from_b64, to_b64},
    database::Database,
    traits::DatabaseT,
    type_aliases::{Bytes, JavaPointer},
};

pub use error::CoreError;

use jni::{
    objects::{JClass, JObject, JString},
    sys::{_jobject, jstring},
    JNIEnv,
};

fn call_core_inner<'a>(
    mut env: JNIEnv<'a>,
    db_java_class: JObject,
    input: JString,
) -> Result<*mut JavaPointer, CoreError> {
    let db = Database::new(&env, db_java_class);

    db.start_transaction()?;
    let input = db.parse_input(input)?;
    let bs = from_b64(&input)?;
    let reversed = bs.iter().rev().cloned().collect::<Bytes>();
    let reversed_b64 = to_b64(&reversed);
    db.call_callback()?;
    db.end_transaction()?;
    db.to_return_value_pointer(&reversed_b64)
    //Ok(env.new_string("poop")?.into_inner())
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "C" fn Java_com_ptokenssentinelandroidapp_RustBridge_callCore(
    env: JNIEnv,
    _class: JClass,
    db_java_class: JObject,
    input: JString,
) -> jstring {
    call_core_inner(env, db_java_class, input)
        .map_err(|e| e.to_string()) // FIXME encode the error type to b64 too
        .expect("this not to panic")
}
