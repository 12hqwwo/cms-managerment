package com.project.WebAloTra.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(
        @Value("${cloudinary.cloud_name}") String cloudName,
        @Value("${cloudinary.api_key}") String apiKey,
        @Value("${cloudinary.api_secret}") String apiSecret
    ) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map options = ObjectUtils.asMap(
            "folder", "Products"
        );
        Map result = cloudinary.uploader().upload(file.getBytes(), options);
        return result.get("secure_url").toString();
    }

    // List image URLs in a Cloudinary folder (e.g., Products/{productCode})
    public List<String> listImagesByFolder(String folder) {
        try {
            Map result = cloudinary.search()
                .expression("folder=" + folder)
                .sortBy("public_id", "desc")
                .maxResults(30)
                .execute();

            List<String> urls = new ArrayList<>();
            Object resourcesObj = result.get("resources");
            if (resourcesObj instanceof List) {
                List resources = (List) resourcesObj;
                for (Object r : resources) {
                    if (r instanceof Map) {
                        Map res = (Map) r;
                        Object secureUrl = res.get("secure_url");
                        Object url = res.get("url");
                        if (secureUrl != null) urls.add(secureUrl.toString());
                        else if (url != null) urls.add(url.toString());
                    }
                }
            }
            return urls;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}