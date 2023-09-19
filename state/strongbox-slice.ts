import {NULL_STRING} from '../lib/constants'
import {createSlice, PayloadAction} from '@reduxjs/toolkit'

const strongboxSlice = createSlice({
  name: 'strongbox',

  initialState: {
    strongboxNumSent: 0,
    strongboxNumRecv: 0,
    strongboxLastSent: NULL_STRING,
    strongboxLastRecv: NULL_STRING,
  },
  reducers: {
    strongboxSetLastSent(state, action: PayloadAction<string>) {
      state.strongboxLastSent = action.payload
      state.strongboxNumSent = state.strongboxNumSent + 1
    },
    strongboxSetLastRecv(state, action: PayloadAction<string>) {
      state.strongboxLastRecv = action.payload
      state.strongboxNumRecv = state.strongboxNumRecv + 1
    },
  },
})

export const {
  strongboxSetLastSent,
  strongboxSetLastRecv,
} = strongboxSlice.actions

export default strongboxSlice.reducer
