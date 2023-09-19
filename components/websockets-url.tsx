import React, {FC, useState} from 'react'
import {useAppDispatch} from '../state/store'
import {DEFAULT_WS_URL} from '../lib/constants'
import {websocketSetUrl} from '../state/websockets-slice'
import {TextInput, Text, View, Pressable} from 'react-native'

interface WsUrlebSocketProps {}

const WebSocketsUrl: FC<WsUrlebSocketProps> = () => {
  const [url, setUrl] = useState(DEFAULT_WS_URL)

  const dispatch = useAppDispatch() // NOTE: Required for hook call rules

  const onSubmitEdit = (): void => {
    console.log(`setting web sockets url in state to '${url}`)
    dispatch(websocketSetUrl(url))
  }

  return (
    <View
      style={{
        flexDirection: 'column',
        justifyContent: 'center',
        backgroundColor: '#A7C7E7',
      }}>
      <TextInput
        value={url}
        maxLength={100}
        onChangeText={setUrl}
        style={{borderWidth: 1}}
      />
      <Pressable
        style={{
          borderWidth: 4,
          borderColor: 'white',
          flexDirection: 'column',
          justifyContent: 'center',
        }}
        onPress={onSubmitEdit}>
        <Text>Press to update ws url in state</Text>
      </Pressable>
    </View>
  )
}

export default WebSocketsUrl
