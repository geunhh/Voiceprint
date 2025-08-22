package com.voiceprint.backend.service.s3;

import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class S3Uploader {
    /**
     * 임시 생성 컴파일 에러 방지
     */
    public String upload(InputStream inputStream, String dirName) {
        // TODO: Implement actual S3 upload logic
        System.out.println("Uploading to S3 in directory: " + dirName);
        return "https://s3.amazonaws.com/your-bucket/" + dirName + "/example.wav";
    }
}
