import { configureStore } from '@reduxjs/toolkit'
import webSocketUrlReducer from './web-sockets-slice'
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux'

// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
type AppDispatch = typeof store.dispatch

export const store = configureStore({
  reducer: {
    webSocketUrl: webSocketUrlReducer
  }
})

// NOTE: See docs: https://react-redux.js.org/using-react-redux/usage-with-typescript
// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>

// NOTE: Typed versions of these fxns
export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
