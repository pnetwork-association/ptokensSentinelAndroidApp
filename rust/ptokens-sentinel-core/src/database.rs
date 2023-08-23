use crate::CoreError;
use derive_more::Constructor;
use jni::objects::{GlobalRef, JObject, JValue};
use jni::{objects::JClass, JNIEnv};

#[derive(Constructor)]
pub struct Database<'a> {
    db_java_class: JClass<'a>,
    //env: &'a JNIEnv<'a>,
    //callback: GlobalRef,
}

impl Database<'_> {
    pub fn call_callback(&self, env: &mut JNIEnv) -> Result<(), CoreError> {
        env.call_static_method(&self.db_java_class, "callback", "()V", &[])?;
        Ok(())
    }
}

/*
impl Database<'_> {
    pub fn open<'a>(env: &'a mut JNIEnv<'a>, callback: JObject) -> Result<Database<'a>, CoreError> {
        // NOTE: We make a global ref of the passed in object to ensure it's GCd by java once we
        // drop it in rust.
        // FIXME Rm Here it seems we're turning this ref back into an obect once the DB has been
        // started. I don't think we need this logic since that object appears to be the
        // `main_activity_instance`, which is the whatever method (in rust or java???) that we're _actually_ calling
        //
        // Real question here is can we pass in the db object so that we only open it once?

        info!("opening database");
        match env.new_global_ref(callback) {
            Ok(global_ref) => {
                let db = Database::new(env, global_ref);
                info!("db opened");
                Ok(db)
            }
            Err(e) => {
                let err_msg = format!(
                    "failed to create the global reference for the java callback object: {e}"
                );
                error!("{err_msg}");
                env.throw_new("java/lang/Exception", format!("{}", err_msg))?;
                Err(e.into())
            }
        }
    }
}

pub fn get_database<'a>(db_class: JClass<'a>) -> Result<Database<'a>, CoreError> {
    Database::open()
}
*/

/*
use ptokens_core::{
    Bytes,
    AppError,
    DatabaseInterface,
    Result as PTokensResult,
};

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
