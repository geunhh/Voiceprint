/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Pretendard", "ui-sans-serif", "system-ui"],
        onemobile: ["ONEMobilePOP", "sans-serif"],
      },
    },
  },
  plugins: [],
};
