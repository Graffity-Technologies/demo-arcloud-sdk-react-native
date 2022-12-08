import React from 'react';
import { NativeModules, Button, requireNativeComponent, UIManager, findNodeHandle, PixelRatio } from 'react-native';

const { GraffityAndroidModule } = NativeModules;

export const AndroidModuleButton = () => {
  const onPress = () => {
    console.log('We will invoke the native module here!');
    GraffityAndroidModule.openARActivity();
  };

  return (
    <Button
      title="Open AR Android"
      onPress={onPress}
    />
  );
};