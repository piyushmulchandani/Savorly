package com.application.savorly.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String uploadImage(MultipartFile file, String newFileName) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", newFileName,
                "overwrite", true
        )).get("secure_url").toString();
    }

    public String uploadPdf(MultipartFile file, String newFileName) throws IOException {
        String uniqueFileName = newFileName;
        if (!uniqueFileName.toLowerCase().endsWith(".pdf")) {
            uniqueFileName += ".pdf";
        }
        uniqueFileName += "-" + System.currentTimeMillis();

        Map<String, Object> params = ObjectUtils.asMap(
                "resource_type", "auto",
                "public_id", uniqueFileName.replace(".pdf", ""),
                "overwrite", true
        );

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        return uploadResult.get("secure_url").toString();
    }
}
