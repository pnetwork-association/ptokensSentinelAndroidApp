import {useAppDispatch} from '../state/store'
import {promisifyCallbackFxn} from '../utils.js'
import {RootState, useAppSelector} from '../state/store'
import {setWebSocketState} from '../state/web-sockets-slice'
import useWebSocket, {ReadyState} from 'react-native-use-websocket'
import React, {FC, useState} from 'react'
import {setLastSent, setLastReceived} from '../state/strongbox-slice'
import {WEB_SOCKET_CONNECTION_STATUS, NULL_STRING} from '../lib/constants'
import { Text, View, StyleSheet, Pressable, NativeModules, Button } from 'react-native'

// NOTE: Docs: https://github.com/Sumit1993/react-native-use-websocket#readme

const callRustCore = (_arg: String) =>
  promisifyCallbackFxn(NativeModules.RustBridge.callRustCore, [_arg])

interface WebSocketProps {}

const WebSockets: FC<WebSocketProps> = () => {
  const [lastMsgSentToStrongbox, setLastMsgSentToStrongbox] = useState(NULL_STRING)
  const [lastMsgReceivedFromStrongbox, setLastMsgReceivedFromStrongbox] = useState(NULL_STRING)

  const [lastMsgSentToWebSocket, setLastMsgSentToWebSocket] = useState(NULL_STRING)
  const [lastMsgReceivedFromWebSocket, setLastMsgReceivedFromWebSocket] = useState(NULL_STRING)

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
    //lastMessage,
    //getWebSocket,
    //sendJsonMessage,
    //lastJsonMessage,
  } = useWebSocket(webSocketUrl, {
      onOpen: () => {
        console.log('web socket opened')
        dispatch(setWebSocketState(ReadyState.OPEN))
      },
      onMessage: ({ data }) => {
        // FIXME set in redux too
        //dispatch(setLastSent(url))
        setLastMsgSentToStrongbox(data)
        setLastMsgReceivedFromWebSocket(data)
        callRustCore(data)
          .then((r: string) => {
            setLastMsgReceivedFromStrongbox(r)
            sendMessage(r)
            setLastMsgSentToWebSocket(r)
          })
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
        flexDirection: 'row',
        justifyContent: 'center',
        backgroundColor: '#fc7f50',
      }}>
      <Pressable onPress={onPress}>
        <Text style={{color: 'white', fontWeight: 'bold'}}>
          WEBSOCKETS STUFF
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`current socket webSocketUrl: '${webSocketUrl}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`current socket state: '${webSocketState}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`last msg sent to websocket: '${lastMsgSentToWebSocket}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`last msg received from websocket: '${lastMsgReceivedFromWebSocket}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`last msg sent to strongbox: '${lastMsgSentToStrongbox}'`}
        </Text>

        <Text style={{color: 'white', fontWeight: 'bold'}}>
          {`last msg received from strongbox: '${lastMsgReceivedFromStrongbox}'`}
        </Text>
      </Pressable>
    </View>
  )
}

export default WebSockets
