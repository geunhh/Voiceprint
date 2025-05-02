import { useState } from "react";
import profileImageEdit from "../../assets/icons/profileImageEdit.png";
import ProfileEditModal from "../../components/modal/ProfileEditModal";

// 유저 정보
const user = {
  userId : 1, 
  userName: '김혜민',
  userImage:"https://i.pinimg.com/736x/a7/ca/36/a7ca369a79ff17fb0ae1c13e72a7a8b4.jpg",
  customThemaId:null
}

export default function EditProfilePage() {
  const [modalOpen, setModalOpen] = useState(false)

  return (
    <div>
      {/* 유저 프로필 및 닉네임 */}
      <div className="mt-10 flex flex-col items-center">
        {/* 유저 프로필 */}
        <div className="relative w-36 h-36">
          <img
            src={user.userImage}
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
        <p className="font-semibold text-2xl text-center mt-3">{user.userName}</p>
      </div>

      {/* 프로필 수정 모달 */}
      {modalOpen && (
        <ProfileEditModal 
          userName={user.userName}
          userImage={user.userImage}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}
