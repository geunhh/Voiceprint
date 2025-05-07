import "./App.css";

function App() {
  return (
    <div className="min-h-dvh w-screen flex justify-center bg-neutral-50">
      {/* ① 데스크톱: 가운데 ② 모바일: width 100% */}
      <div className="w-full max-w-[393px] min-h-dvh bg-white shadow-lg">
        <h1 className="text-3xl font-bold text-blue-600">
          Tailwind 연결 완료! 🎉
        </h1>
      </div>
    </div>
  );
}

export default App;
