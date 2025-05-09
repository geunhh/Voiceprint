import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface UserState {
  userId: number | null;
  nickname: string;
  imageUrl: string;
}

const initialState: UserState = {
  userId: null,
  nickname: "",
  imageUrl: "",
};

const userSlice = createSlice({
  name: "user",
  initialState,
  reducers: {
    setUser(state, action: PayloadAction<UserState>) {
      return action.payload;
    },
    clearUser() {
      return initialState;
    },
  },
});

export const { setUser, clearUser } = userSlice.actions;
export default userSlice.reducer;
