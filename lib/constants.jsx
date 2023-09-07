import{ReadyState} from 'react-native-use-websocket'

const DEFAULT_WS_URL = 'ws://localhost:3000/ws'

const WEB_SOCKET_CONNECTION_STATUS = {
  [ReadyState.OPEN]: 'Open',
  [ReadyState.CLOSED]: 'Closed',
  [ReadyState.CLOSING]: 'Closing',
  [ReadyState.CONNECTING]: 'Connecting',
  [ReadyState.UNINSTANTIATED]: 'Uninstantiated',
}

const NULL_STRING = 'null'

export {
  NULL_STRING,
  DEFAULT_WS_URL,
  WEB_SOCKET_CONNECTION_STATUS,
}
