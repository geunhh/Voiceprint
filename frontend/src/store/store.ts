import { configureStore } from "@reduxjs/toolkit";
import characterReducer from "./characterSlice";

// localStorage 저장
const saveToLocalStorage = (state: any) => {
  try {
    const serializedState = JSON.stringify(state);
    localStorage.setItem("character", serializedState);
  } catch (e) {
    console.error("Could not save state", e);
  }
};

// localStorage 불러오기
const loadFromLocalStorage = () => {
  try {
    const serializedState = localStorage.getItem("character");
    if (serializedState === null) {
      return undefined;
    }
    return { character: JSON.parse(serializedState) };
  } catch (e) {
    console.error("Could not load state", e);
    return undefined;
  }
};

export const store = configureStore({
  reducer: {
    character: characterReducer,
  },
  preloadedState: loadFromLocalStorage(), // localStorage에서 불러오기
});

store.subscribe(() => {
  saveToLocalStorage(store.getState().character); // 상태가 바뀔 때마다 저장
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
