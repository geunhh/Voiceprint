import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface CharacterState {
  img: string;
  name: string;
  tag: string;
}

const initialState: CharacterState = {
  img: "",
  name: "",
  tag: "",
};

const characterSlice = createSlice({
  name: "character",
  initialState,
  reducers: {
    setCharacter(state, action: PayloadAction<CharacterState>) {
      return action.payload;
    },
    resetCharacter() {
      return initialState;
    },
  },
});

export const { setCharacter, resetCharacter } = characterSlice.actions;
export default characterSlice.reducer;
