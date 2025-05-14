import { format } from "date-fns";
import { ko } from "date-fns/locale";

interface GroupCardProps {
  groupName: string;
  groupImageUrl: string;
  memberCount: number;
  memberProfileImages: string[];
  createdAt: string;
  onClick: () => void;
}

function GroupCard({
  groupName,
  groupImageUrl,
  memberCount,
  memberProfileImages,
  createdAt,
  onClick,
}: GroupCardProps) {
  const displayed = memberProfileImages.slice(0, 3);

  return (
    <div className="w-[70vw] max-w-[300px] h-[50vh] max-h-[600px] mx-auto mt-5 mb-5 group-card">
      {/* 카드 클릭 영역 */}
      <div
        className="rounded-tr-xl rounded-br-xl overflow-hidden shadow-md flex flex-col h-full cursor-pointer"
        onClick={onClick}
      >
        {/* 이미지 영역 */}
        <div className="relative w-full flex-1 overflow-hidden">
          <img
            src={groupImageUrl}
            alt="group"
            className="absolute w-full h-full object-cover"
          />
        </div>

        {/* 텍스트 영역 */}
        <div className="bg-white px-4 py-2">
          <p className="font-bold text-lg">{groupName}</p>
          <p className="text-sm text-gray-500 mt-1">
            {memberCount}명이 함께 하는 중
          </p>
          <div className="flex justify-end mt-2 gap-1">
            {displayed.map((url, idx) => (
              <img
                key={idx}
                src={url}
                alt={`member-${idx}`}
                className="w-8 h-8 rounded-full"
              />
            ))}
          </div>
        </div>
      </div>

      {/* 카드 바깥에 위치한 생성일 */}
      <p className="text-sm text-gray-500 text-center mt-2 mb-4">
        생성일 {format(new Date(createdAt), "yyyy.MM.dd", { locale: ko })}
      </p>
    </div>
  );
}

export default GroupCard;
