#![cfg(target_os="android")]
use jni::{
    objects::{JByteArray, JObject, JClass, JString, JByteBuffer},
    sys::{jbyteArray, jstring, jarray, jobject},
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
    input: JByteArray<'local>,
) -> jbyteArray {
    let len = env.get_array_length(&input).expect("should get length of array");
    let mut input_buffer = vec![0i8; len as usize];
    let start_idx = 0;
    env.get_byte_array_region(&input, start_idx, &mut input_buffer).expect("could not get byte array from input");

    let u8slice = unsafe { &*(&input_buffer[..] as *const [i8] as *const [u8]) };
    **env.byte_array_from_slice(u8slice).unwrap()

    /*
    let output = env
        .new_string("test from rust".to_string())
        .expect("Couldn't create java string!");
    //let x: JByteArray = vec![6,6,6].into();
    let l = 4;
    let output = env.new_byte_array(l).unwrap(); //Fixme

    output.into_raw()
    */
}
