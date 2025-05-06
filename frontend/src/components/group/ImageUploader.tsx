import { useRef, useState } from "react";

interface ImageUploaderProps {
  defaultImage: string;
  onImageChange?: (imageDataUrl: string) => void;
}

export default function ImageUploader({
  defaultImage,
  onImageChange,
}: ImageUploaderProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [imageSrc, setImageSrc] = useState<string>(defaultImage);

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === "string") {
        setImageSrc(reader.result);
        onImageChange?.(reader.result);
      }
    };
    reader.readAsDataURL(file);
  };

  return (
    <div>
      <img
        src={imageSrc}
        alt="이미지 업로드"
        className="w-20 h-20 rounded-full cursor-pointer object-cover"
        onClick={handleImageClick}
      />
      <input
        type="file"
        accept="image/*"
        ref={fileInputRef}
        onChange={handleFileChange}
        className="hidden"
      />
    </div>
  );
}
