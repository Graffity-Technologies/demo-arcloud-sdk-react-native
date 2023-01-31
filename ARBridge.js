import React, { useEffect, useState } from 'react';
import { NativeModules, Button, NativeEventEmitter } from 'react-native';

const { GraffityAndroidModule } = NativeModules;

export const AndroidModuleButton = () => {
  const [result, setResult] = useState();

  const onPress = async () => {
    let res = await GraffityAndroidModule.openARActivity();
    setResult(res);
  };

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    eventListener = eventEmitter.addListener('message', (event) => {
      console.log("eventListener", event.eventProperty)
    });

    return () => eventListener.remove();
  });

  // useEffect(() => {
  //   setInterval(async () => {
  //     let touchedNodeId = await GraffityAndroidModule.getTouchedNodeId();
  //     console.log("touchedNodeId", touchedNodeId)
  //   }, 2000);
  // }, []);

  useEffect(() => {
    console.log("result", result);
  }, [result])

  return (
    <Button
      title="Open AR Android"
      onPress={onPress}
    />
  );
};