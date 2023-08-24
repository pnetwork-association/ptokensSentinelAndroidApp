use crate::{
    traits::DatabaseT,
    type_aliases::{ByteArray, Bytes, DataSensitivity, JavaPointer},
    CoreError,
};
use derive_more::Constructor;
use jni::{
    objects::{GlobalRef, JClass, JObject, JString, JValue},
    JNIEnv,
};

const DB_CLASS_PATH: &str = "com/ptokenssentinelandroidapp/database/DatabaseWiring";

#[derive(Constructor)]
pub struct Database<'a> {
    env: JNIEnv<'a>,
    //db_java_class: JClass<'a>,
    //callback: GlobalRef,
}

impl Database<'_> {
    pub fn parse_input(&mut self, input: &JString) -> Result<String, CoreError> {
        Ok(self.env.get_string(input)?.into())
    }

    fn to_jstring(&mut self, s: &str) -> Result<JString<'_>, CoreError> {
        Ok(self.env.new_string(s)?)
    }

    pub fn to_return_value_pointer(&mut self, s: &str) -> Result<*mut JavaPointer, CoreError> {
        Ok(self.to_jstring(s)?.into_raw())
    }

    pub fn call_callback(&mut self) -> Result<(), CoreError> {
        let class = self.env.find_class(DB_CLASS_PATH)?;
        self.env.call_static_method(class, "callback", "()V", &[])?;
        Ok(())
    }
}

/*
impl DatabaseInterface for Database<'_> {
    fn end_transaction(&self) -> PTokensResult<()> {
        let main_activity_instance = self.callback.as_obj();

        match self.env.call_method(
            main_activity_instance,
            "endTransaction",
            "()V",
            &[]
        ) {
            Ok(_) => Ok(()),
            Err(e) => {
                self.env.exception_describe().unwrap();
                self.env.exception_clear().unwrap();
                Err(AppError::Custom(e.to_string()))
            }
        }
    }

    fn start_transaction(&self) -> PTokensResult<()> {
        let main_activity_instance = self.callback.as_obj();
        match self.env.call_method(
            main_activity_instance,
            "startTransaction",
            "()V",
            &[]
        ) {
            Ok(_) => Ok(()),
            Err(e) => {
                self.env.exception_describe().unwrap();
                self.env.exception_clear().unwrap();
                Err(AppError::Custom(e.to_string()))
            }
        }
    }

    fn delete(&self, key: Bytes) -> PTokensResult<()> {
        let main_activity_instance = self.callback.as_obj();
        let key_byte_array = match self.env.byte_array_from_slice(&key) {
            Ok(v) => Some(v),
            Err(e) => {
                error!("✘ {}", e);
                None
            }
        };

        if key_byte_array.is_none() {
            return Err(AppError::Custom("Failed to get the key byte array from JAVA in delete()".to_string()))
        }

        match self.env.call_method(
            main_activity_instance,
            "delete",
            "([B)V",
            &[JValue::from(JObject::from(key_byte_array.unwrap()))]
        ) {
            Ok(_) => Ok(()),
            Err(e) => {
                self.env.exception_describe().unwrap();
                self.env.exception_clear().unwrap();
                Err(AppError::Custom(e.to_string()))
            }
        }
    }

    fn get(&self, key: Bytes, sensitivity: Option<u8>) -> PTokensResult<Bytes> {
        let jmain_activity = self.callback.as_obj();
        let jkey = match self.env
            .byte_array_from_slice(&key) {
            Ok(v) => Some(v),
            Err(e) => {
                error!("✘ {}", e);
                None
            }
        };

        if jkey.is_none() {
            return Err(AppError::Custom("Failed to get the key byte array from JAVA in get()".to_string()))
        }

        match self.env.call_method(
            jmain_activity,
            "get",
            "([BB)[B",
            &[
                JValue::from(JObject::from(jkey.unwrap())),
                JValue::from(sensitivity.unwrap_or(0))
            ]
        ) {
            Ok(v) => {
                let maybe_java_value = match v.l() {
                    Ok(v) => Some(v),
                    Err(e) => {
                        error!("✘ {}", e);
                        None
                    }
                };

                let value = match self.env
                    .convert_byte_array(maybe_java_value.unwrap().into_inner()) {
                    Ok(v) => Some(v),
                    Err(e) => {
                        error!("✘ {}", e);
                        None
                    }
                };

                if value.is_none() {
                    return Err(AppError::Custom("Failed to convert to byte array in get()".to_string()))
                }

                Ok(value.unwrap())
            },
            Err(e) => {
                self.env.exception_describe().unwrap();
                self.env.exception_clear().unwrap();
                Err(AppError::Custom(e.to_string()))
            }
        }
    }

    fn put(&self, key: Bytes, value: Bytes, sensitivity: Option<u8>) -> PTokensResult<()> {
        let main_activity_instance = self.callback.as_obj();
        let key_byte_array = match self.env
            .byte_array_from_slice(&key) {
            Ok(v) => Some(v),
            Err(e) => {
                error!("✘ {}", e);
                None
            }
        };

        let value_byte_array = match self.env
            .byte_array_from_slice(&value) {
            Ok(v) => Some(v),
            Err(e) => {
                error!("✘ {}", e);
                None
            }
        };

        if value_byte_array.is_none() || key_byte_array.is_none() {
            return Err(AppError::Custom("Invalid arguments for put()".to_string()))
        }

        match self.env.call_method(
            main_activity_instance,
            "put",
            "([B[BB)V",
            &[
                JValue::from(JObject::from(key_byte_array.unwrap())),
                JValue::from(JObject::from(value_byte_array.unwrap())),
                JValue::from(sensitivity.unwrap_or(0))
            ]
        ) {
            Ok(_) => Ok(()),
            Err(e) => {
                self.env.exception_describe().unwrap();
                self.env.exception_clear().unwrap();
                Err(AppError::Custom(e.to_string()))
            }
        }
    }
}
*/
