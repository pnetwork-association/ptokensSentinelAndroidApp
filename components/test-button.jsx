import React from 'react';
import {NativeModules, Button} from 'react-native';
import {promisifyCallbackFxn} from '../utils.js';

const callCore = _arg =>
  promisifyCallbackFxn(NativeModules.RustBridge.callCore, [_arg]);

const TestButton = () => {
  const onPress = () => {
    callCore([1, 2, 3, 4]).then(_r => console.log('here', _r));
  };

  return (
    <Button title="click to call core" color="#fc8eac" onPress={onPress} />
  );
};

export default TestButton;
