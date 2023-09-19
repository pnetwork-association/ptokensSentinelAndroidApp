import {
  NULL_STRING,
  DEFAULT_WS_URL,
  WEB_SOCKET_CONNECTION_STATUS,
} from '../lib/constants'
import {ReadyState} from 'react-native-use-websocket'
import {createSlice, PayloadAction} from '@reduxjs/toolkit'

const websocketSlice = createSlice({
  name: 'websocket',

  initialState: {
    websocketNumSent: 0,
    websocketNumRecv: 0,
    websocketUrl: DEFAULT_WS_URL,
    websocketLastSent: NULL_STRING,
    websocketLastRecv: NULL_STRING,
    websocketState: WEB_SOCKET_CONNECTION_STATUS[ReadyState.UNINSTANTIATED],
  },

  reducers: {
    websocketSetUrl(state, action: PayloadAction<string>) {
      state.websocketUrl = action.payload
    },
    websocketSetState(state, action: PayloadAction<ReadyState>) {
      state.websocketState = WEB_SOCKET_CONNECTION_STATUS[action.payload]
    },
    websocketSetLastSent(state, action: PayloadAction<string>) {
      state.websocketNumSent = state.websocketNumSent + 1
      state.websocketLastSent = action.payload
    },
    websocketSetLastRecv(state, action: PayloadAction<string>) {
      state.websocketNumRecv = state.websocketNumRecv + 1
      state.websocketLastRecv = action.payload
    },
  },
})

export const {
  websocketSetUrl,
  websocketSetState,
  websocketSetLastRecv,
  websocketSetLastSent,
} = websocketSlice.actions

export default websocketSlice.reducer
