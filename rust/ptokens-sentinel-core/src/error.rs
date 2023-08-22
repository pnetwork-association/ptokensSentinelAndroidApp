use thiserror::Error;

#[derive(Debug, Error)]
pub enum CoreError {
    #[error("custom: {0}")]
    Custom(String),

    #[error("jni error: {0}")]
    Jni(#[from] jni::errors::Error),

    #[error("base64 error: {0}")]
    Base64(#[from] base64::DecodeError),
}
