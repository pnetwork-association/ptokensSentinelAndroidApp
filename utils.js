const promisifyCallbackFxn = (_fxn, _args = []) =>
  new Promise(resolve => _fxn(..._args, _r => resolve(_r)));

export {promisifyCallbackFxn};
