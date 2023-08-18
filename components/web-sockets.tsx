import {useAppDispatch} from '../state/store'
import {RootState, useAppSelector} from '../state/store'
import {setWebSocketState} from '../state/web-sockets-slice'
import {WEB_SOCKET_CONNECTION_STATUS} from '../lib/constants'
import useWebSocket, {ReadyState} from 'react-native-use-websocket'
import React, {useMemo, useRef, FC, useEffect, useState} from 'react'
import { Text, View, StyleSheet, Pressable, NativeModules, Button } from 'react-native'

// NOTE: Docs: https://github.com/Sumit1993/react-native-use-websocket#readme

interface WebSocketProps {}

const WebSockets: FC<WebSocketProps> = () => {
  const [info, setInfo] = useState('nothing yet')

  const dispatch = useAppDispatch()

  const getRandomNum = (): number => {
    return Math.floor(Math.random() * 10)
  }

  const {
    url: webSocketUrl,
    socketState: webSocketState,
  } = useAppSelector((state: RootState) => state.webSocket)

  const {
    readyState,
    sendMessage,
    lastMessage,
    getWebSocket,
    sendJsonMessage,
    lastJsonMessage,
  } = useWebSocket(webSocketUrl, {
      onOpen: () => {
        console.log('web socket opened')
        dispatch(setWebSocketState(ReadyState.OPEN))
      },
      onMessage: ({ data }) => {
        console.log('message received: ', data)
        setInfo(data)
      },
      onClose: ({ reason }) => {
        console.log(`web socket closed reason: ${reason}`)
        dispatch(setWebSocketState(ReadyState.CLOSED))
      },
      shouldReconnect: () => true,
      reconnectAttempts: Number.MAX_SAFE_INTEGER,
      reconnectInterval: 10e3,
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
          {`current socket webSocketUrl: '${webSocketUrl}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`current socket state: '${webSocketState}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`current info from socket: '${info}'`}
        </Text>
      </Pressable>
    </View>
  )
}

export default WebSockets
