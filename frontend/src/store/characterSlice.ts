import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface CharacterState {
  id: number; // ← id 필드 추가
  img: string;
  name: string;
  tag: string;
}

const initialState: CharacterState = {
  id: 0,
  img: "",
  name: "",
  tag: "",
};

const characterSlice = createSlice({
  name: "character",
  initialState,
  reducers: {
    setCharacter: (_state, action: PayloadAction<CharacterState>) =>
      action.payload,
    resetCharacter: () => initialState,
  },
});

export const { setCharacter, resetCharacter } = characterSlice.actions;
export default characterSlice.reducer;
