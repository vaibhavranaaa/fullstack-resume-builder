package in.vaibhavrana.resumebuilderapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import in.vaibhavrana.resumebuilderapi.document.Resume;
import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import in.vaibhavrana.resumebuilderapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;
    private final AuthService authService;
    private final ResumeRepository resumeRepository;

    public Map<String, String> uploadSingleImage(MultipartFile file) throws IOException {

        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "image")
        );

        String secureUrl = uploadResult.get("secure_url").toString();
        log.info("Image uploaded successfully: {}", secureUrl);

        return Map.of("imageUrl", secureUrl);
    }

    public Map<String, String> uploadResumeImages(String resumeId,
                                                  Object principal,
                                                  MultipartFile thumbnail,
                                                  MultipartFile profileImage) {

        try {
            // Step 1: Get logged-in user
            AuthResponse response = authService.getProfile(principal);

            // Step 2: Fetch resume (ownership check)
            Resume existingResume = resumeRepository
                    .findByUserIdAndId(response.getId(), resumeId)
                    .orElseThrow(() -> new RuntimeException("Resume not found"));

            Map<String, String> returnValue = new HashMap<>();

            // Step 3: Upload thumbnail
            if (Objects.nonNull(thumbnail) && !thumbnail.isEmpty()) {
                Map<String, String> uploadResult = uploadSingleImage(thumbnail);
                existingResume.setThumbnailLink(uploadResult.get("imageUrl"));
                returnValue.put("thumbnailLink", uploadResult.get("imageUrl"));
            }

            // Step 4: Upload profile image
            if (Objects.nonNull(profileImage) && !profileImage.isEmpty()) {
                Map<String, String> uploadResult = uploadSingleImage(profileImage);

                if (Objects.isNull(existingResume.getProfileInfo())) {
                    existingResume.setProfileInfo(new Resume.ProfileInfo());
                }

                existingResume.getProfileInfo()
                        .setProfilePreviewUrl(uploadResult.get("imageUrl"));

                returnValue.put("profilePreviewUrl", uploadResult.get("imageUrl"));
            }

            // Step 5: Save changes
            resumeRepository.save(existingResume);

            returnValue.put("message", "Images uploaded successfully");
            return returnValue;

        } catch (IOException e) {
            log.error("Image upload failed", e);
            throw new RuntimeException("Image upload failed");
        }
    }
}
