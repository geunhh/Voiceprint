import { useState } from "react";
import { useSelector } from "react-redux";
import profileImageEdit from "../../assets/icons/edit.png";
import ThemaList from "../../components/common/ThemaList";
import ProfileEditModal from "../../components/modal/ProfileEditModal";
import { RootState } from "../../store/store";

export default function EditProfilePage() {
  const [modalOpen, setModalOpen] = useState(false);
  const user = useSelector((state: RootState) => state.user);

  return (
    <div>
      {/* 유저 프로필 및 닉네임 */}
      <div className="mt-10 flex flex-col items-center">
        {/* 유저 프로필 */}
        <div className="relative w-36 h-36">
          <img
            src={user.imageUrl}
            className="rounded-full w-36 h-36 object-cover"
            alt="프로필 이미지"
          />
          <img
            src={profileImageEdit}
            alt="프로필 수정"
            className="absolute bottom-1 right-1 w-10"
            onClick={() => setModalOpen(true)}
          />
        </div>
        {/* 닉네임 */}
        <p className="font-semibold text-2xl text-center mt-3">
          {user.nickname}
        </p>
      </div>

      {/* 테마 변경 */}
      <div className="mt-5 mb-0 thema-list-padding">
        <p className="ml-4 font-semibold text-gray-500">일기 테마 수정하기</p>
        {/* 테마 리스트 */}
        <ThemaList />
      </div>

      {/* 프로필 수정 모달 */}
      {modalOpen && (
        <ProfileEditModal
          userName={user.nickname}
          userImage={user.imageUrl}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}
