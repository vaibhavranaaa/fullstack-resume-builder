package in.vaibhavrana.resumebuilderapi.Controller;
import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import in.vaibhavrana.resumebuilderapi.dto.RegisterRequest;
import in.vaibhavrana.resumebuilderapi.service.AuthService;
import in.vaibhavrana.resumebuilderapi.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static in.vaibhavrana.resumebuilderapi.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)

public class AuthController {
    private final AuthService authService;
    private final FileUploadService fileUploadService;

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        log.info("Inside AuthController-register(): {}",request);
        AuthResponse response=authService.register(request);
        log.info("Response from service:{}",response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
    @GetMapping(VERIFY)
    public ResponseEntity<?> verifyEmail(@RequestParam String token){
        log.info("Inside AuthController-verifyEmail() : {}",token);
        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Email verified succesfully"));
    }

    @PostMapping(UPLOADIMAGE)
    public ResponseEntity<?> uploadImage(@RequestPart("image")MultipartFile file) throws IOException {
        log.info("Inside AuthController-uploadImage()");
        Map<String,String> response=fileUploadService.uploadSingleImage(file);
        return ResponseEntity.ok(response);

    }
}
