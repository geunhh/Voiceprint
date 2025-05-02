import React from "react";
import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center h-dvh gap-4">
      <h1 className="text-4xl font-bold">404</h1>
      <p className="text-lg">페이지를 찾을 수 없습니다.</p>
      <Link
        to="/"
        className="px-4 py-2 rounded-md bg-blue-600 text-white hover:bg-blue-700"
      >
        홈으로 이동
      </Link>
    </div>
  );
}
