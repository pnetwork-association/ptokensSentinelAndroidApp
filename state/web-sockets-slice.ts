import {DEFAULT_WS_URL} from '../lib/constants'
import{ReadyState} from 'react-native-use-websocket'
import {createSlice, PayloadAction} from '@reduxjs/toolkit'
import {WEB_SOCKET_CONNECTION_STATUS} from '../lib/constants'

const webSocketSlice = createSlice({
  name: 'webSocket',

  initialState: {
    url: DEFAULT_WS_URL,
    socketState: WEB_SOCKET_CONNECTION_STATUS[ReadyState.UNINSTANTIATED],
  },
  reducers: {
    setWebSocketUrl(state, action: PayloadAction<string>) {
      state.url = action.payload
    },
    setWebSocketState(state, action: PayloadAction<ReadyState>) {
      state.socketState = WEB_SOCKET_CONNECTION_STATUS[action.payload]
    },
  },
})

export const {setWebSocketUrl, setWebSocketState} = webSocketSlice.actions

export default webSocketSlice.reducer
