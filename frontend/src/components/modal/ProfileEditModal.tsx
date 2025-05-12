import axios from "axios";
import { useEffect, useState } from "react";
import closeIcon from "../../assets/icons/close.png";
import profileSelect from "../../assets/icons/profileSelect.png";
import Button from "../common/Button";

import { useDispatch } from "react-redux";
import { updateUser } from "../../store/userSlice";

interface ProfileEditProps {
  userName: string;
  userImage: string;
  onClose: () => void;
}

interface ProfileImage {
  id: number;
  title: string;
  imageUrl: string;
}

function ProfileEditModal({ userName, userImage, onClose }: ProfileEditProps) {
  const [selectedImage, setSelectedImage] = useState(userImage);
  const [nickname, setNickname] = useState(userName);
  const [isFocused, setIsFocused] = useState(false);
  const [profileList, setProfileList] = useState<ProfileImage[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const dispatch = useDispatch();

  useEffect(() => {
    const fetchProfileImages = async () => {
      try {
        const res = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/api/v1/user/profileimage`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
              "Content-Type": "application/json",
            },
          }
        );
        setProfileList(res.data.data);

        const current = res.data.data.find(
          (img: ProfileImage) => img.imageUrl === userImage
        );
        if (current) setSelectedId(current.id);
      } catch (error) {
        console.error("프로필 이미지 목록 불러오기 실패", error);
      }
    };

    fetchProfileImages();
  }, [userImage]);

  const handleSave = async () => {
    const updatedNickname = nickname.trim() || userName;
    const updatedImageId = selectedId;

    try {
      await axios.patch(
        `${import.meta.env.VITE_API_BASE_URL}/api/v1/user/profile`,
        {
          nickname: updatedNickname,
          profileImageId: updatedImageId,
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );

      const newImageUrl = profileList.find(
        (img) => img.id === updatedImageId
      )?.imageUrl;

      dispatch(
        updateUser({
          nickname: updatedNickname,
          imageUrl: newImageUrl,
        })
      );
      console.log(updatedNickname);
      onClose();
    } catch (error) {
      console.error("프로필 업데이트 실패", error);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center">
      <div className="w-4/5 max-w-[320px] rounded-xl bg-white flex flex-col items-center py-6 relative">
        {/* 닫기 버튼 */}
        <img
          src={closeIcon}
          alt="닫기버튼"
          className="w-6 absolute top-4 right-4 cursor-pointer"
          onClick={onClose}
        />

        {/* 제목 */}
        <p className="text-xl font-bold mb-4">프로필 수정하기</p>

        {/* 선택된 프로필 */}
        <div className="w-28 h-28 rounded-full flex items-center justify-center mb-2">
          <img
            src={selectedImage}
            alt="선택된 프로필"
            className="w-28 h-28 object-cover rounded-full"
          />
        </div>

        {/* 이름 */}
        <input
          type="text"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          className={`text-lg text-center font-semibold border-b w-2/5 mb-6 outline-none transition 
            ${isFocused ? "border-yellow-400" : "border-gray-300"}`}
          placeholder={nickname}
        />

        {/* 프로필 이미지 목록 */}
        <div className="grid grid-cols-3 gap-4 mb-6 px-4">
          {profileList.map((img, idx) => (
            <div
              key={idx}
              className={`relative w-20 h-20 rounded-full flex items-center justify-center cursor-pointer `}
              onClick={() => {
                setSelectedImage(img.imageUrl);
                setSelectedId(img.id);
              }}
            >
              <img
                src={img.imageUrl}
                alt={`프로필${idx}`}
                className="w-20 h-20 object-cover rounded-full"
              />
              {selectedImage === img.imageUrl && (
                <img
                  src={profileSelect}
                  alt="선택"
                  className="absolute -top-1 right-0 w-7 h-7"
                />
              )}
            </div>
          ))}
        </div>

        {/* 저장 버튼 */}
        <div className="w-full px-6 mt-auto flex justify-center">
          <Button type="fill" size="M" text="저장" onClick={handleSave} />
        </div>
      </div>
    </div>
  );
}

export default ProfileEditModal;
