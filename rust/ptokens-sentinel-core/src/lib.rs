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
    sys::{jstring},
    JNIEnv,
};

fn call_core_inner(
    env: JNIEnv<'_>,
    db_java_class: JObject,
    input: JString,
) -> Result<*mut JavaPointer, CoreError> {
    let db = Database::new(&env, db_java_class);

    db.start_transaction()?;

    let k = vec![6u8, 6u8, 7u8];
    //let v = vec![1u8, 3u8, 3u8, 7u8];
    //db.put(&k, &v, None)?;
    let x = match db.get(&k, None) {
        Ok(r) => r,
        Err(e) => {
            println!("{e}");
            vec![9u8, 9u8, 9u8]
        }
    };

    let input = db.parse_input(input)?;
    let bs = from_b64(&input)?;
    let reversed = bs.iter().rev().cloned().collect::<Bytes>();
    let _reversed_b64 = to_b64(&reversed);
    db.call_callback()?;
    db.end_transaction()?;
    db.to_return_value_pointer(&to_b64(&x))
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
