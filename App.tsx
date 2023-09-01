import {Provider} from 'react-redux'
import {store} from './state/store'
import React from 'react'
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  useColorScheme,
  View,
} from 'react-native'

import DropDbButton from './components/drop-db'
import WebSockets from './components/web-sockets'
import TestButton from './components/test-button.jsx'
import WebSocketsUrl from './components/web-sockets-url'
import {Colors} from 'react-native/Libraries/NewAppScreen'

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark'

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  }

  return (
    <Provider store={store}>
      <SafeAreaView style={backgroundStyle}>
        <StatusBar
          barStyle={isDarkMode ? 'light-content' : 'dark-content'}
          backgroundColor={backgroundStyle.backgroundColor}
        />
        <TestButton />
        <WebSockets />
        <WebSocketsUrl />
        <DropDbButton />
        <ScrollView
          contentInsetAdjustmentBehavior="automatic"
          style={backgroundStyle}>
          <View
            style={{
              backgroundColor: isDarkMode ? Colors.black : Colors.white,
            }}
          />
        </ScrollView>
      </SafeAreaView>
    </Provider>
  )
}

export default App
