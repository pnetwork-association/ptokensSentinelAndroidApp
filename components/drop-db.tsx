import React from 'react'
import {NativeModules, Button} from 'react-native'

const dropDb = () =>
  new Promise<void>((resolve, reject) =>
    NativeModules.RustBridge.dropDb(
      () => resolve(),
      (e: string) => reject(e)
    )
  )

const DropDbButton = () => {
  const onPress = () =>
    dropDb()
      .then(_ => console.log('db got dropped'))
      .catch(console.error)

  return <Button title="click to drop the db" color="#7c0a02" onPress={onPress} />
}

export default DropDbButton
