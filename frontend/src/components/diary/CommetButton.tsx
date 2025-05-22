function CommentButton({ onClick }: { onClick: () => void }) {
  return (
    <button onClick={onClick} className="rounded-2xl bg-yellow-50 p-1 w-20">
      <p className="text-center text-yellow-400 font-semibold">댓글</p>
    </button>
  );
}
export default CommentButton;
