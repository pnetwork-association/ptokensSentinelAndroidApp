import React from 'react';
import {NativeModules, Button} from 'react-native';

const {RustBridge} = NativeModules;

const TestButton = () => {
  const onPress = () => {
    console.log('here', RustBridge);
    RustBridge.callCore([1, 2, 3, 4], r => console.log('result', r));
  };

  return (
    <Button
      title="Click to invoke your native module!"
      color="#841584"
      onPress={onPress}
    />
  );
};

export default TestButton;
