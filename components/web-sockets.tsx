import {RootState, useAppSelector} from '../state/store'
import React, {useMemo, useRef, FC, useEffect, useState} from 'react'
import {
  Text,
  View,
  StyleSheet,
  Pressable,
  NativeModules,
  Button,
} from 'react-native'
import useWebSocket, {ReadyState} from 'react-native-use-websocket'

interface WebSocketProps {}

// NOTE: This is the react-native version (https://www.npmjs.com/package/react-native-use-websocket)
// but the non react-native version has the docs: https://www.npmjs.com/package/use-websocket

const WebSockets: FC<WebSocketProps> = () => {
  const [info, setInfo] = useState('nothing yet')

  const getRandomNum = (): number => {
    return Math.floor(Math.random() * 10)
  }

  const {webSocketUrl} = useAppSelector(
    (state: RootState) => state.webSocketUrl,
  )

  const connectionStatus = {
    [ReadyState.OPEN]: 'Open',
    [ReadyState.CLOSED]: 'Closed',
    [ReadyState.CLOSING]: 'Closing',
    [ReadyState.CONNECTING]: 'Connecting',
    [ReadyState.UNINSTANTIATED]: 'Uninstantiated',
  }

  const {
    readyState,
    sendMessage,
    lastMessage,
    getWebSocket,
    sendJsonMessage,
    lastJsonMessage,
  } = useWebSocket(webSocketUrl, {
      onOpen: () => console.log('web socket opened'),
      onMessage: e => {
        console.log('message received: ', e.data)
        setInfo(e.data)
      },
      onClose: () => console.log('web socket closed'),
      shouldReconnect: closeEvent => true,
    })

  const sendMsg = (_msg: string) => sendMessage(_msg)

  const onPress = (): void => {
    console.log('web sockets test button')
    console.log('ready state: ', readyState)
    if (readyState === ReadyState.OPEN) {
      sendMsg(`random num: ${getRandomNum()}`)
    } else {
      console.warn('cannot send message - web socket not open')
    }
  }

  return (
    <View
      style={{
        borderWidth: 10,
        borderRadius: 50,
        borderColor: 'white',
        flexDirection: 'row',
        justifyContent: 'center',
        backgroundColor: '#fc7f50',
      }}>
      <Pressable onPress={onPress}>
        <Text style={{color: 'white', fontWeight: 'bold'}}>
          WEBSOCKETS BUTTON
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`current socket url: '${webSocketUrl}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`current info from socket: '${info}'`}
        </Text>
      </Pressable>
    </View>
  )
}

export default WebSockets
