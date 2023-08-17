import { configureStore } from '@reduxjs/toolkit'
import webSocketUrlReducer from './web-sockets-slice'

export const store = configureStore({
  reducer: {
    webSocketUrl: webSocketUrlReducer
  }
})

// NOTE: See docs: https://react-redux.js.org/using-react-redux/usage-with-typescript
// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch
