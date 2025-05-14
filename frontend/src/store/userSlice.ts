import { createSlice } from "@reduxjs/toolkit";

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
    setUser(state, action) {
      const { userId, nickname, imageUrl } = action.payload;
      state.userId = userId;
      state.nickname = nickname;
      state.imageUrl = imageUrl;
    },
    updateUser(state, action) {
      const { nickname, imageUrl } = action.payload;
      state.nickname = nickname;
      state.imageUrl = imageUrl;
    },
  },
});

export const { setUser, updateUser } = userSlice.actions;
export default userSlice.reducer;
