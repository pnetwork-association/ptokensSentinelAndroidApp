import React from 'react';
import {NativeModules, Button} from 'react-native';
import {promisifyCallbackFxn} from '../utils.js';

const callRustCore = _arg =>
  promisifyCallbackFxn(NativeModules.RustBridge.callRustCore, [_arg]);

const TestButton = () => {
  const onPress = () => {
    let b64Str = 'AQMDkBw'
    callRustCore(b64Str).then(_r => console.log('here', _r));
  };

  return (
    <Button title="click to call core" color="#fc8eac" onPress={onPress} />
  );
};

export default TestButton;
