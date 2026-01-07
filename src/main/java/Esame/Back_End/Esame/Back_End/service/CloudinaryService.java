package Esame.Back_End.Esame.Back_End.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    
    private final Cloudinary cloudinary;
    
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
            "folder", folder,
            "resource_type", "image",
            "overwrite", true,
            "transformation", new Object[]{
                ObjectUtils.asMap("width", 1000, "height", 1000, "crop", "limit")
            }
        );
        
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) result.get("secure_url");
    }
    
    public String uploadProfileImage(MultipartFile file) throws IOException {
        return uploadImage(file, "cinema/profiles");
    }
    
    public String uploadMoviePoster(MultipartFile file) throws IOException {
        return uploadImage(file, "cinema/posters");
    }
    
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        }
    }
    
    private String extractPublicId(String url) {
        try {
            int startIndex = url.lastIndexOf("/") + 1;
            int endIndex = url.lastIndexOf(".");
            if (startIndex > 0 && endIndex > startIndex) {
                String filename = url.substring(startIndex, endIndex);
                // Remove version prefix if present
                if (url.contains("/v")) {
                    String[] parts = url.split("/");
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].equals("v") && i + 1 < parts.length) {
                            return parts[i + 1] + "/" + filename;
                        }
                    }
                }
                return filename;
            }
        } catch (Exception e) {
            // If extraction fails, return null
        }
        return null;
    }
}

