import PageTitle from "../../components/common/PageTitle";
import ThemaList from "../../components/common/ThemaList";

export default function DiaryThemePage() {
  return (
    <div className="flex flex-col items-center bg-white">
      <div className="w-[95%] mx-auto pb-24">
        <PageTitle
          title="일기 설정하기"
          subtitle="생성될 일기의 어투를 설정해주세요"
        />

        <div className="flex-1 overflow-y-auto">
          <ThemaList />
        </div>
      </div>
    </div>
  );
}
