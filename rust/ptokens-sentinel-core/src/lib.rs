#![cfg(target_os = "android")]

#[macro_use]
extern crate log;

mod database;
mod error;

use self::database::Database;
pub use error::CoreError;

use jni::{
    objects::{JClass, JString},
    sys::jstring,
    JNIEnv,
};

use base64::{engine::general_purpose as b64_codec, Engine};

pub type Bytes = Vec<u8>;
pub type ByteArray = [u8];

pub fn call_core(bytes: &[u8]) -> Bytes {
    bytes.iter().rev().cloned().collect()
}

fn from_b64<T: AsRef<ByteArray>>(s: &T) -> Result<Bytes, CoreError> {
    Ok(b64_codec::STANDARD_NO_PAD.decode(s)?)
}

fn to_b64(bs: &ByteArray) -> String {
    b64_codec::STANDARD_NO_PAD.encode(bs)
}

fn call_core_inner<'a>(env: &'a mut JNIEnv, input: &'a JString) -> Result<JString<'a>, CoreError> {
    let input: String = env.get_string(input)?.into();
    let bs = from_b64(&input)?;
    let reversed = call_core(&bs);
    let reversed_b64 = to_b64(&reversed);
    Ok(env.new_string(reversed_b64)?)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "C" fn Java_com_ptokenssentinelandroidapp_RustBridge_callCore<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    input: JString<'local>,
) -> jstring {
    //Database::open(&env, ??).unwrap(); // FIXME

    /*
    let input: String = env
        .get_string(&input)
        .expect("could not get java string")
        .into();
    let bs = from_b64(&input).unwrap();
    let reversed = call_core(&bs);
    let r = to_b64(&reversed);
    */

    let class = env
        .find_class("com/ptokenssentinelandroidapp/database/DatabaseWiring")
        .expect("Failed to load the target class");

    let result = env.call_static_method(class, "callback", "()V", &[]).unwrap(); // FIXME
    //debug!(">>>>>>>> here is the result: {result:?}");

    //env.new_string(format!("{class:?}")) // JClass(JObject { internal: 0x99, lifetime: PhantomData<&()> }) // not helpful
    //env.new_string(r)
    /*
    env.new_string(format!("{result:?}"))
        .expect("could not create java string")
        .into_raw()
    */

    call_core_inner(&mut env, &input)
        .map_err(|e| e.to_string()) // FIXME encode the error type to b64 too
        .expect("this won't panic")
        .into_raw() // NOTE: This gets a pointer to the jstring for the JVM
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn should_encode_b64_correctly() {
        let bs = vec![1, 3, 3, 7];
        let result = to_b64(bs);
        let expected_result = "AQMDBw";
        assert_eq!(result, expected_result);
    }

    #[test]
    fn should_decode_b64_correctly() {
        let s = "AQMDBw";
        let result = from_b64(s.into());
        let expected_result = vec![1, 3, 3, 7];
        assert_eq!(result, expected_result);
    }
}
