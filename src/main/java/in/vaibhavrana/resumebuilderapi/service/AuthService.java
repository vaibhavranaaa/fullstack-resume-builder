package in.vaibhavrana.resumebuilderapi.service;

import in.vaibhavrana.resumebuilderapi.document.User;
import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import in.vaibhavrana.resumebuilderapi.dto.RegisterRequest;
import in.vaibhavrana.resumebuilderapi.exception.ResourceExistsException;
import in.vaibhavrana.resumebuilderapi.repository.UserRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRespository userRespository;

    public AuthResponse register(RegisterRequest request){
        log.info("Inside AuthService: register() {}",request);

        if(userRespository.existsByEmail(request.getEmail())){
            throw new ResourceExistsException("User already existes with this email");
        }

        User newUser=toDocument(request);

        userRespository.save(newUser);

        //TODO: send verficatipn email

        return toResponse(newUser);

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
}
