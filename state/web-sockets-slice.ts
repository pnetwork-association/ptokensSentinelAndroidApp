import { DEFAULT_WS_URL } from '../lib/constants'
import { createSlice, PayloadAction } from "@reduxjs/toolkit"

const webSocketSlice = createSlice({
  name: "webSocketUrl",

  initialState: {
    webSocketUrl: DEFAULT_WS_URL
  },
  reducers: {
    setWebSocketUrl(state, action: PayloadAction<string>) {
      state.webSocketUrl = action.payload
    }
  }
})

export const { setWebSocketUrl } = webSocketSlice.actions

export default webSocketSlice.reducer
