import useTimeAgo from "../../hooks/useTimeAgo";

interface CommentProps {
  from: "author" | "others";
  text: string;
  userImage: string;
  userName: string;
  createdAt?: string;
}

export default function CommentBubble({
  from,
  text,
  userImage,
  userName,
  createdAt,
}: CommentProps) {
  const timeAgo = useTimeAgo(createdAt || "");

  if (from === "author") {
    return (
      <div className="flex justify-end items-end">
        <div className="bg-yellow-100 px-4 py-2 rounded-xl max-w-[80%] text-sm whitespace-pre-wrap">
          {text}
        </div>
        <img
          src={userImage}
          alt={userName}
          className="w-8 h-8 rounded-full ml-2"
        />
      </div>
    );
  }

  return (
    <div className="flex items-start">
      <img
        src={userImage}
        alt={userName}
        className="w-8 h-8 rounded-full mr-2"
      />
      <div className="flex flex-col space-y-1">
        <div className="text-xs text-gray-500">
          <span className="font-semibold text-gray-700 mr-1">{userName}</span>
          <span>{timeAgo}</span>
        </div>
        <div className="bg-lightmint px-4 py-2 rounded-xl max-w-[80%] text-sm whitespace-pre-wrap">
          {text}
        </div>
      </div>
    </div>
  );
}
