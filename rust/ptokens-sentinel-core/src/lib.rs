#![cfg(target_os = "android")]
use jni::{
    objects::{JClass, JString},
    sys::jstring,
    JNIEnv,
};

use base64::{engine::general_purpose as b64_codec, Engine};
pub fn call_core(bytes: &[u8]) -> Vec<u8> {
    bytes.iter().rev().cloned().collect()
}

fn from_b64(s: String) -> Vec<u8> {
    b64_codec::STANDARD_NO_PAD.decode(s).unwrap() // FIXME
}

fn to_b64(bs: Vec<u8>) -> String {
    b64_codec::STANDARD_NO_PAD.encode(bs)
}

#[allow(non_snake_case)]
#[no_mangle]
pub extern "C" fn Java_com_ptokenssentinelandroidapp_RustBridge_callCore<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    input: JString<'local>,
) -> jstring {
    let input: String = env
        .get_string(&input)
        .expect("could not get java string")
        .into();
    let bs = from_b64(input);
    let reversed = call_core(&bs);
    let r = to_b64(reversed);
    env.new_string(r)
        .expect("could not create java string")
        .into_raw()
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
