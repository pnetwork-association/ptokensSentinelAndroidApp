import React from 'react';
import {NativeModules, Button} from 'react-native';

const {RustBridge} = NativeModules;

// NOTE: We have to pass callbacks to native modules to get return values. This wrapper just promisifies them.
const RUST_BRIDGE_WRAPPER = {
  callCore: _arg =>
    new Promise(resolve => RustBridge.callCore(_arg, _r => resolve(_r))),
};

const TestButton = () => {
  const onPress = () => {
    RUST_BRIDGE_WRAPPER.callCore([1, 2, 3, 4]).then(_r =>
      console.log('here', _r),
    );
  };

  return (
    <Button title="click to call core" color="#fc8eac" onPress={onPress} />
  );
};

export default TestButton;
