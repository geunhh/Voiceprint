import { configureStore } from "@reduxjs/toolkit";
import characterReducer from "./characterSlice";
import userReducer from "./userSlice";

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
    user: userReducer,
  },
  preloadedState: loadFromLocalStorage(), // localStorage에서 불러오기
});

store.subscribe(() => {
  saveToLocalStorage(store.getState().character);
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
