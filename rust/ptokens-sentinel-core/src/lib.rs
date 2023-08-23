#![cfg(target_os = "android")]

#[macro_use]
extern crate log;

mod base64;
mod traits;
mod database;
mod error;
mod type_aliases;

use self::{
    base64::{from_b64, to_b64},
    database::Database,
    type_aliases::Bytes,
};

pub use error::CoreError;

use jni::{
    objects::{JClass, JString},
    sys::jstring,
    JNIEnv,
};

fn call_core_inner<'a>(env: &'a mut JNIEnv, input: &'a JString) -> Result<JString<'a>, CoreError> {
    let db_class_path = "com/ptokenssentinelandroidapp/database/DatabaseWiring";
    let db = Database::new(env.find_class(db_class_path)?);
    db.call_callback(env)?; // FIXME rm!

    let input: String = env.get_string(input)?.into();
    let bs = from_b64(&input)?;
    let reversed = bs.iter().rev().cloned().collect::<Bytes>();
    let reversed_b64 = to_b64(&reversed);
    Ok(env.new_string(reversed_b64)?)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "C" fn Java_com_ptokenssentinelandroidapp_RustBridge_callCore<'a>(
    mut env: JNIEnv<'a>,
    _class: JClass<'a>,
    input: JString<'a>,
) -> jstring {
    call_core_inner(&mut env, &input)
        .map_err(|e| e.to_string()) // FIXME encode the error type to b64 too
        .expect("this not to panic")
        .into_raw() // NOTE: This gets a pointer to the jstring to return to the JVM
}
