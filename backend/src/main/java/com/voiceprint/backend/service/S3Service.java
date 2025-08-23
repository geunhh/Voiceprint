package com.voiceprint.backend.service;

import com.voiceprint.backend.global.exception.s3.InvalidFileException;
import com.voiceprint.backend.global.exception.s3.S3UnavailableException;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("업로드 파일이 유효하지 않습니다");
        }

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        // 폴더 구분
        folder = folder + "/";
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folder + fileName)
                .contentType(file.getContentType())
                .build();
        try {
            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드 성공 시 URL 반환
            return getFileUrl(folder, fileName);
        } catch (Exception e) {
            throw new S3UnavailableException("파일 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * S3 버킷 내 특정 폴더와 파일명을 기반으로 파일 URL을 생성하는 메서드
     *
     * @param folder 폴더 이름 (예: "profile/", "permanent/")
     * @param fileName 파일 이름
     * @return String 생성된 파일 URL
     */
    public String getFileUrl(String folder, String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s%s",
                bucketName,
                s3Client.serviceClientConfiguration().region(),
                folder,
                fileName);
    }

    public void deleteFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String key = url.getPath().substring(1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new S3UnavailableException("파일 삭제 실패: " + e.getMessage());
        }
    }
}
