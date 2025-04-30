import { useSelector } from "react-redux";
import { RootState } from "../../store/store";
import ProgressBar from "../../components/common/ProgressBar";
import Button from "../../components/common/Button";
import { useNavigate } from "react-router-dom";

export default function DiaryVoicePage() {
  const character = useSelector((state: RootState) => state.character);
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-4 pt-12 pb-36">
      <div className="flex flex-col items-center gap-8">
        {/* 캐릭터 원 */}
        <div className="w-60 h-60 rounded-full flex items-center justify-center">
          <img
            src={character.img}
            alt={character.name}
            className="w-64 h-64 object-contain"
          />
        </div>

        {/* 텍스트 */}
        <div className="text-gray-400 text-base text-center">
          AI의 대답이 들어가요
        </div>

        <div className="flex flex-col items-center gap-6 w-full">
          {/* 진행바 */}
          <div className="w-full max-w-[320px]">
            <ProgressBar label="" progress={30} />
          </div>
        </div>

        {/* 버튼 */}
        <div
          className="
              fixed bottom-[20vh] left-1/2 -translate-x-1/2
              w-[90vw] max-w-[320px]
              flex justify-center
          "
        >
          <Button
            text="일기 생성하기"
            type="fill"
            size="L"
            onClick={() => navigate("/diary/temp")}
          />
        </div>
      </div>
    </div>
  );
}
