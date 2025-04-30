import { useSelector } from "react-redux";
import { RootState } from "../../store/store";
import ProgressBar from "../../components/common/ProgressBar";
import Button from "../../components/common/Button";

export default function DiaryVoicePage() {
  const character = useSelector((state: RootState) => state.character);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white">
      {/* 캐릭터 이미지 */}
      <img src={character.img} alt={character.name} className="w-64 h-64" />

      {/* AI 응답 텍스트 */}
      <div className="mt-8 text-gray-500 text-base text-center">
        AI의 대답이 들어가요
      </div>

      {/* 진행바 */}
      <div className="w-72 m-10">
        <ProgressBar label="" progress={68} />
      </div>

      {/* 버튼을 맨 아래로 밀기 */}
      <div className="w-full mt-auto mb-40 flex justify-center">
        <Button
          text="일기 생성하기"
          type="fill"
          size="L"
          onClick={() => console.log("일기 생성")}
        />
      </div>
    </div>
  );
}
