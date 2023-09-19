import {
  websocketSetState,
  websocketSetLastRecv,
  websocketSetLastSent,
} from '../state/websockets-slice'
import React, {FC} from 'react'
import {useAppDispatch} from '../state/store'
import {Text, View, NativeModules} from 'react-native'
import {RootState, useAppSelector} from '../state/store'
import useWebSocket, {ReadyState} from 'react-native-use-websocket'
import {strongboxSetLastSent, strongboxSetLastRecv} from '../state/strongbox-slice'

// NOTE: Docs: https://github.com/Sumit1993/react-native-use-websocket#readme

const callRustCore = (_arg: string) =>
  new Promise<string>((resolve, reject) =>
    NativeModules.RustBridge.callRustCore(
      _arg,
      (_r: string) => resolve(_r),
      (_e: string) => reject(_e),
    )
 )

const doDbOp = (_open: boolean) =>
  new Promise<void>((resolve, reject) =>
    NativeModules.RustBridge[`${_open ? 'open' : 'close'}Db`](
      () => resolve(),
      (_e: string) => reject(_e),
    )
 )

const openDb = (): Promise<void> => doDbOp(true)
const closeDb = (): Promise<void> => doDbOp(false)

interface WebSocketProps {}

const WebSockets: FC<WebSocketProps> = () => {
  const dispatch = useAppDispatch()

  const sliceDataForDisplay = (_s: String) => {
    let n = 10
    return `${_s.slice(0, n)}...${_s.slice(-n)}`
  }

  const {
    websocketUrl,
    websocketState,
    websocketNumSent,
    websocketNumRecv,
    websocketLastSent,
    websocketLastRecv,
  } = useAppSelector((state: RootState) => state.websocket)

  const {
    strongboxNumSent,
    strongboxNumRecv,
    strongboxLastSent,
    strongboxLastRecv,
  } = useAppSelector((state: RootState) => state.strongbox)

  const {
    sendMessage,
    /* readyState,
    lastMessage,
    getWebSocket,
    sendJsonMessage,
    lastJsonMessage,
     */
  } = useWebSocket(websocketUrl, {
      onOpen: () => {
        console.log('websocket opened')
        dispatch(websocketSetState(ReadyState.OPEN))
        openDb() // FIXME what to do in case this rejects?
      },
      onMessage: ({ data }) => {
        // FIXME set in redux too
        let slicedData = sliceDataForDisplay(data)
        dispatch(strongboxSetLastSent(slicedData))
        dispatch(websocketSetLastRecv(slicedData))
        callRustCore(data)
          .then((_data: string) => {
            slicedData = sliceDataForDisplay(_data)
            dispatch(strongboxSetLastRecv(slicedData))
            dispatch(websocketSetLastSent(slicedData))
            sendMessage(_data)
          })
          .catch((_e: string) => {
            console.error(`calling rust core returned an error: ${_e}`)
            dispatch(websocketSetLastSent(_e))
            sendMessage(_e)
          })
      },
      onClose: ({ reason }) => {
        console.log(`web socket closed reason: ${reason}`)
        dispatch(websocketSetState(ReadyState.CLOSED))
        closeDb() // FIXME what to do if this rejects?
      },
      shouldReconnect: () => true,
      reconnectAttempts: Number.MAX_SAFE_INTEGER,
      reconnectInterval: 10e3,
    })

  return (
    <View
      style={{
        flexDirection: 'column',
        justifyContent: 'center',
        backgroundColor: '#fc7f50',
      }}>
      <Text style={{color: 'white', fontWeight: 'bold'}}>
        WEBSOCKETS STUFF
      </Text>

      <Text style={{color: 'white', fontWeight: 'bold'}}>
        {`current socket websocketUrl: '${websocketUrl}'`}
        {'\n'}
        {`web socket state: ${websocketState}`}
        {'\n'}
        {'\n'}
        {`num strongbox msgs sent: ${strongboxNumSent}`}
        {'\n'}
        {`num strongbox msgs recv: ${strongboxNumRecv}`}
        {'\n'}
        {`last strongbox msg sent: '${strongboxLastSent}'`}
        {'\n'}
        {`last strongbox msg recv: '${strongboxLastRecv}'`}
        {'\n'}
        {'\n'}
        {`num websocket msgs sent: ${websocketNumSent}`}
        {'\n'}
        {`num websockets msgs recv: ${websocketNumRecv}`}
        {'\n'}
        {`last websocket msg sent: '${websocketLastRecv}'`}
        {'\n'}
        {`last websocket msg recv: '${websocketLastSent}'`}
        {'\n'}
      </Text>
    </View>
  )
}

export default WebSockets
