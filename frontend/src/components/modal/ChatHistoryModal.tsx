import closeIcon from "../../assets/icons/close.png";
import { ChatBubbleProps } from "../chat/ChatBubble";
import ChatList from "../chat/ChatList";

interface ChatHistoryModalProps {
  onClose: () => void;
}

// 임시 데이터
const messages: ChatBubbleProps[] = [
  {
    from: "ai",
    text: "안녕~ 오늘 하루는 어땠는지 나한테 이야기 해줘!\n이야기를 듣고 내가 일기를 대신 작성해줄게!",
  },
  {
    from: "user",
    text: "오늘 나는 친구들이랑 같이 한강 공원에 놀러갔어!\n날씨가 너무 좋아서 바람도 솔솔솔 불고\n햇빛도 적당히 따뜻해서 기분이 좋아지더라고.\n그리고 한강하면 엽떡에 허니콤보잖아.\n엽떡에 유부 추가 3번 하고\n시원한 맥주랑 같이 먹으니까 완벽한 하루였어!",
  },
  {
    from: "ai",
    text: "응응 너무 재미있었겠다! 나도 요즘 날씨가 좋아서 어디론가 놀러가고 싶어져~.",
  },
  {
    from: "user",
    text: "그리고 시원한 밤공기와 함께 따릉이를 타고 놀았어!\n지금같은 날씨에 한강 공원 돌려가며\n따릉이 타고 무조건 달려야 하잖아.\n시원한 바람을 맞으면서 자전거를 타니까\n하루 마무리가 정말 기분 좋았어.",
  },
  {
    from: "ai",
    text: "또 기억에 남는 일이 있을까?",
  },
  {
    from: "user",
    text: "밤도깨비 야시장도 다녀왔어! 푸드트럭에서 파는 닭꼬치, 다꼬야끼, 회오리감자 등등 맛있는 음식을 한번에 먹을 수 있으니까 정말 좋더라구!",
  },
  {
    from: "ai",
    text: "시원한 한강에서 먹는 맛있는 음식은 언제나 맛있지~",
  },
];

function ChatHistoryModal({ onClose }: ChatHistoryModalProps) {
  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center">
      <div className="w-4/5 max-w-[320px] rounded-xl bg-white flex flex-col py-6 relative max-h-[90vh] overflow-y-auto">
        {/* 닫기 버튼 */}
        <img
          src={closeIcon}
          alt="닫기버튼"
          className="w-6 absolute top-4 right-4 cursor-pointer"
          onClick={onClose}
        />

        {/* 제목 */}
        <p className="text-xl font-bold text-center mb-4">대화 기록</p>

        {/* 채팅 리스트 */}
        <div className="h-[60vh] px-4">
          <ChatList messages={messages} />
        </div>
      </div>
    </div>
  );
}

export default ChatHistoryModal;
