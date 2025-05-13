/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Pretendard", "ui-sans-serif", "system-ui"],
        onemobile: ["ONEMobilePOP", "sans-serif"],
      },
      colors: {
        mint: "#B5E6E2", // 민트색 등록
        darkmint: "#8DC4BF", // 어두운 민트색 등록
        lightmint: "#DEF6F5", // 밝은 민트색 등록
      },
      keyframes: {
        /* 둥실둥실 이동 */
        float: {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-10px)" },
        },
        /* 좌우 살짝 흔들 */
        shake: {
          "0%, 100%": { transform: "translateX(0)" },
          "15%": { transform: "translateX(-3px) rotate(-2deg)" },
          "30%": { transform: "translateX(3px)  rotate(2deg)" },
          "45%": { transform: "translateX(-3px) rotate(-2deg)" },
          "60%": { transform: "translateX(3px)  rotate(2deg)" },
          "75%": { transform: "translateX(-2px) rotate(-1deg)" },
        },
      },
      animation: {
        /* 이름:  키프레임 시간 타이밍 옵션 */
        float: "float 3s ease-in-out infinite",
        shake: "shake 2s linear infinite",
      },
    },
  },
  plugins: [],
};
