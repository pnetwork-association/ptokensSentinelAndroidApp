import{ReadyState} from 'react-native-use-websocket'
import {createSlice, PayloadAction} from '@reduxjs/toolkit'
import {DEFAULT_WS_URL, NULL_STRING} from '../lib/constants'
import {WEB_SOCKET_CONNECTION_STATUS} from '../lib/constants'

const strongboxSlice = createSlice({
  name: 'strongbox',

  initialState: {
    lastSent: NULL_STRING,
    lastReceived: NULL_STRING,
  },
  reducers: {
    setLastSent(state, action: PayloadAction<string>) {
      state.lastSent = action.payload
    },
    setLastReceived(state, action: PayloadAction<string>) {
      state.lastReceived = action.payload
    },
  },
})

export const {setLastSent, setLastReceived} = strongboxSlice.actions

export default strongboxSlice.reducer
