import React from 'react'
import {NativeModules, Button} from 'react-native'
import {promisifyCallbackFxn} from '../utils.js'

const dropDb = _ =>
  promisifyCallbackFxn(NativeModules.RustBridge.dropDb, [])

const DropDbButton = () => {
  const onPress = () => {
    let b64Str = 'AQMDkBw'
    dropDb().then(_ => console.log('db got dropped'))
  }

  return <Button title="click to drop the db" color="#7c0a02" onPress={onPress} />
}

export default DropDbButton
