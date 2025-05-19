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
        mint: "#B5E6E2", // 민트색
        darkmint: "#8DC4BF", // 어두운 민트색
        lightmint: "#DEF6F5", // 밝은 민트색

        customyellow: "#FFC700", // 기본 노란색(Figma_yellow500)
        lightyellow: "#FFF6D4", // 밝은 노란색(Figma_yellow100)
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
        /* 테두리 애니메이션 효과 */
        shimmer: {
          "0%": { backgroundPosition: "0% 50%" },
          "100%": { backgroundPosition: "150% 50%" },
        },
      },
      animation: {
        /* 이름:  키프레임 시간 타이밍 옵션 */
        float: "float 3s ease-in-out infinite",
        shake: "shake 2s linear infinite",
        shimmer: "shimmer 3s linear infinite",
      },
    },
  },
  plugins: [],
};
