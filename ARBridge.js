import React from 'react';
import { NativeModules, Button } from 'react-native';

const { GraffityAndroidModule } = NativeModules;

export const AndroidModuleButton = () => {
  const onPress = () => {
    GraffityAndroidModule.openARActivity();
  };

  return (
    <Button
      title="Open AR Android"
      onPress={onPress}
    />
  );
};