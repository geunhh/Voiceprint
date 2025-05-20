import { useRef, useState } from "react";
import uploadIcon from "../../assets/icons/upload.png";
import AlertModal from "../../components/modal/AlertModal";

interface ImageUploaderProps {
  defaultImage: string;
  onImageChange?: (file: File) => void;
}

export default function ImageUploader({
  defaultImage,
  onImageChange,
}: ImageUploaderProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [imageSrc, setImageSrc] = useState<string>(defaultImage);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const MAX_SIZE_MB = 5;
    const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

    if (file.size > MAX_SIZE_BYTES) {
      setIsModalOpen(true);
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === "string") {
        setImageSrc(reader.result);
        onImageChange?.(file);
      }
    };
    reader.readAsDataURL(file);
  };

  return (
    <>
      <div
        className="w-full max-w-md h-40 bg-gray-100 border border-dashed border-gray-300 rounded-lg flex items-center justify-center cursor-pointer overflow-hidden"
        onClick={handleImageClick}
      >
        {imageSrc ? (
          <img
            src={imageSrc}
            alt="업로드 이미지"
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="flex flex-col items-center justify-center gap-2">
            <img src={uploadIcon} alt="업로드 아이콘" className="h-12" />
            <p className="text-gray-500">그룹 이미지를 업로드해 주세요</p>
          </div>
        )}
        <input
          type="file"
          accept="image/*"
          ref={fileInputRef}
          onChange={handleFileChange}
          className="hidden"
        />
      </div>

      {isModalOpen && (
        <AlertModal
          type="fail"
          message={"이미지는 5MB 이하로 \n선택해서 업로드해 주세요!"}
          onClose={() => setIsModalOpen(false)}
        />
      )}
    </>
  );
}
