import {
  NULL_STRING,
  DEFAULT_WS_URL,
  WEB_SOCKET_CONNECTION_STATUS,
} from '../lib/constants'
import{ReadyState} from 'react-native-use-websocket'
import {createSlice, PayloadAction} from '@reduxjs/toolkit'

const webSocketSlice = createSlice({
  name: 'webSocket',

  initialState: {
    url: DEFAULT_WS_URL,
    lastMsgSent: NULL_STRING,
    lastMsgReceived: NULL_STRING,
    socketState: WEB_SOCKET_CONNECTION_STATUS[ReadyState.UNINSTANTIATED],
  },
  reducers: {
    setWebSocketUrl(state, action: PayloadAction<string>) {
      state.url = action.payload
    },
    setWebSocketState(state, action: PayloadAction<ReadyState>) {
      state.socketState = WEB_SOCKET_CONNECTION_STATUS[action.payload]
    },
    setLastMsgSent(state, action: PayloadAction<string>) {
      state.lastMsgSent = action.payload
    },
    setLastMsgReceived(state, action: PayloadAction<string>) {
      state.lastMsgReceived = action.payload
    },
  },
})

export const {
  setLastMsgSent,
  setWebSocketUrl,
  setWebSocketState,
  setLastMsgReceived,
} = webSocketSlice.actions

export default webSocketSlice.reducer
