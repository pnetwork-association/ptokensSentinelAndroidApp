#![cfg(target_os="android")]

use jni::{
    objects::{JByteArray, JObject, JClass, JString, JByteBuffer},
    sys::{jstring, jbyteArray, jobject},
    JNIEnv,
};

pub fn call_core(bytes: &[u8]) -> Vec<u8> {
    bytes.iter().rev().cloned().collect()
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "C" fn Java_com_ptokenssentinelandroidapp_RustBridge_callCore<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    input: JObject<'local>,
) -> jstring {
    let len = env.get_array_length(&input).expect("should get length of array");
    let mut buffer = vec![0i8; len as usize];
    let start_idx = 0;

    env.get_byte_array_region(&input, start_idx, &mut buffer).expect("Couldn't get java string!");

    println!("passed in bytes: {:?}", buffer);

    let output = env
        .new_string("test from rust".to_string())
        .expect("Couldn't create java string!");

    output.into_raw()
}
