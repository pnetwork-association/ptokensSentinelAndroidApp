import { DEFAULT_WS_URL } from '../lib/constants'
import { setWebSocketUrl } from '../state/web-sockets-slice'
import React, { useMemo, useRef, FC, useEffect, useState } from 'react'
import { RootState, useAppDispatch, useAppSelector } from '../state/store'
import {TouchableHighlight, TextInput, SafeAreaView, Text, View, StyleSheet, Pressable, NativeModules, Button} from 'react-native'
import useWebSocket from 'react-native-use-websocket';

interface WsUrlebSocketProps {}

const WebSocketsUrl: FC<WsUrlebSocketProps> = () => {
  const [url, setUrl] = useState(DEFAULT_WS_URL)

  const dispatch = useAppDispatch();

  const { webSocketUrl } = useAppSelector((state: RootState) => state.webSocketUrl);

  const onSubmitEdit = (): void => {
    console.log(`setting web sockets url in state to '${url}`)
    dispatch(setWebSocketUrl(url));
  }

  return (
    <View
      style={{
        flexDirection: 'column',
        justifyContent: 'center',
        backgroundColor: '#A7C7E7',
      }}
    >
      <TextInput
        value={url}
        maxLength={100}
        onChangeText={setUrl}
        style={{ borderWidth: 1 }}
      />
      <Pressable
        style={{
          borderWidth: 4,
          borderColor: 'white',
          flexDirection: 'column',
          justifyContent: 'center'
        }}
        onPress={onSubmitEdit}
      >
        <Text>Press to update ws url in state</Text>
      </Pressable>
    </View>
  )
}

export default WebSocketsUrl
