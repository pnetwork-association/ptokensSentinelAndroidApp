use crate::{
    type_aliases::{ByteArray, Bytes},
    CoreError,
};

use base64::{engine::general_purpose as b64_codec, Engine};

pub fn from_b64<T: AsRef<ByteArray> + std::convert::AsRef<[u8]>>(
    s: &T,
) -> Result<Bytes, CoreError> {
    Ok(b64_codec::STANDARD_NO_PAD.decode(s)?)
}

pub fn to_b64(bs: &ByteArray) -> String {
    b64_codec::STANDARD_NO_PAD.encode(bs)
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
