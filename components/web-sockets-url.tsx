import React, { useMemo, useRef, FC, useEffect, useState } from 'react'
import {TouchableHighlight, TextInput, SafeAreaView, Text, View, StyleSheet, Pressable, NativeModules, Button} from 'react-native'
import useWebSocket from 'react-native-use-websocket';

interface WsUrlebSocketProps {}
const DEFAULT_WS_URL = 'ws://localhost:3000/ws';

const WebSocketsUrl: FC<WsUrlebSocketProps> = () => {
  const [wsUrl, setWsUrl] = useState(DEFAULT_WS_URL)

  const onSubmitEdit = (): void => {
    console.log('web sockets url currently: ', wsUrl)
    // TODO put in a redux store
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
        value={wsUrl}
        maxLength={100}
        onChangeText={setWsUrl}
        style={{ borderWidth: 1 }}
      />
      <Pressable
        onPress={onSubmitEdit}
        style={{
          borderWidth: 4,
          borderColor: 'white',
          flexDirection: 'column',
          justifyContent: 'center'
        }}
      >
        <Text>Press to update ws url</Text>
      </Pressable>
    </View>
  )
}

export default WebSocketsUrl
