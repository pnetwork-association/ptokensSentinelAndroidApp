use crate::type_aliases::{Bytes, ByteArray, DataSensitivity};

pub trait DatabaseT<T: AsRef<ByteArray> + std::convert::AsRef<[u8]>> {
    type Error;

    fn end_transaction(&self) -> Result<(), Self::Error>;
    fn start_transaction(&self) -> Result<(), Self::Error>;
    fn delete(&self, key: T) -> Result<(), Self::Error>;
    fn get(&self, key: T, data_sensitivity: DataSensitivity) -> Result<Bytes, Self::Error>;
    fn put(&self, key: T, value: T, data_sensitivity: DataSensitivity) -> Result<(), Self::Error>;
}

