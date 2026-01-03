package in.vaibhavrana.resumebuilderapi.service;

import in.vaibhavrana.resumebuilderapi.document.User;
import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import in.vaibhavrana.resumebuilderapi.dto.RegisterRequest;
import in.vaibhavrana.resumebuilderapi.exception.ResourceExistsException;
import in.vaibhavrana.resumebuilderapi.repository.UserRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRespository userRespository;
    private final EmailService emailService;
    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request) {

        log.info("Inside AuthService: register() {}",request);

        if(userRespository.existsByEmail(request.getEmail())){
            throw new ResourceExistsException("User already exists with this email");
        }

        User newUser=toDocument(request);

        userRespository.save(newUser);

        sendVerificationEmail(newUser);


        return toResponse(newUser);

    }

    private void sendVerificationEmail(User newUser) {
        log.info("Inside AuthService -sendVerificationEmail():{}",newUser);
        try {
            String link= appBaseUrl+"/api/auth/verify-email?token="+newUser.getVerificationToken();
            String html =
                    "<div style='font-family: sans-serif;'>" +
                            "<h2>Verify your email</h2>" +
                            "<p>Hi " + newUser.getName() + ", please confirm your email to activate your account.</p>" +

                            "<a href='" + link + "' " +
                            "style='display:inline-block; padding:10px 16px; background:#6366f1; " +
                            "color:#fff; text-decoration:none; border-radius:6px;'>" +
                            "Verify Email</a>" +

                            "<p>Or copy this link:</p>" +
                            "<p style='word-break: break-all;'>" + link + "</p>" +
                            "<p>This link expires in 24 hours.</p>" +
                            "</div>";

            emailService.sendHtmlEmail(newUser.getEmail(),"Verify your email",html);


        }catch(Exception e){
            log.error("Exception occured at sendVerificationEmail():{}",e.getMessage());
            throw new RuntimeException("Failed to send verification email:"+e.getMessage());
        }
    }

    public AuthResponse toResponse(User newUser){
        return AuthResponse.builder()
                .id(newUser.getName())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .profileImageUrl(newUser.getProfileImageUrl())
                .emailVerified(newUser.isEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .createAt(newUser.getCreateAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();

    }
    private User toDocument(RegisterRequest request){
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .profileImageUrl(request.getImageUrl())
                .subscriptionPlan("Basic")
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    public void verifyEmail(String token){
        log.info("Inside AuthService :verifyEmail(): {}",token);
        User user=userRespository.findByVerificationToken(token)
                .orElseThrow(()->new RuntimeException("Invalid or expired verification token"));

        if(user.getVerificationExpires()!=null && user.getVerificationExpires().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Verification token has expired.Please request new one");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRespository.save(user);

    }
}
