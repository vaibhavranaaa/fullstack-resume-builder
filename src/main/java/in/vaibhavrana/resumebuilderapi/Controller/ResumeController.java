package in.vaibhavrana.resumebuilderapi.Controller;

import in.vaibhavrana.resumebuilderapi.document.Resume;
import in.vaibhavrana.resumebuilderapi.dto.CreateResumeRequest;
import in.vaibhavrana.resumebuilderapi.service.FileUploadService;
import in.vaibhavrana.resumebuilderapi.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static in.vaibhavrana.resumebuilderapi.util.AppConstants.*;

@RestController
@RequestMapping(RESUME)
@RequiredArgsConstructor
@Slf4j

public class ResumeController {

    private final ResumeService resumeService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request,
                                          Authentication authentication){
        //STEP1: Call the service method
        Resume newResume=resumeService.createResume(request,authentication.getPrincipal());

        //STEP2: return response
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);


    }
    @GetMapping
    public ResponseEntity<?> getUserResumes(Authentication authentication){
        //Step1: Call th service method
        List<Resume> resumes=resumeService.getUserResumes(authentication.getPrincipal());


        //Step 2:return the response
        return ResponseEntity.ok(resumes);



    }
    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,
                                           Authentication authentication) {
        //Step 1: Call the service method
        Resume existingresume = resumeService.getResumeById(id, authentication.getPrincipal());

        //Step2: return the response
        return ResponseEntity.ok(existingresume);
    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id,@RequestBody Resume updatedData,Authentication authentication){
        //Step1: Call the service method
        Resume updatedResume=resumeService.updateResume(id,updatedData,authentication.getPrincipal());

        //Step2: return response
        return ResponseEntity.ok(updatedResume);
    }

    @PutMapping(UPLOAD_IMAGES)
    public ResponseEntity<?> uploadResumeImaged(@PathVariable String id,
                                                @RequestPart(value="thumbnail",required=false)MultipartFile thumbnail,
                                                @RequestPart(value="profileImage",required=false)MultipartFile profileImage,
                                                Authentication authentication){
        //Step1: call the service method
        Map<String,String> response=fileUploadService.uploadResumeImages(id,authentication.getPrincipal(),thumbnail,profileImage);

        //Step2: return the response
        return ResponseEntity.ok(response);

    }
    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id,
                                          Authentication authentication){

        //Step 1: call the service method
        resumeService.deleteResume(id,authentication.getPrincipal());

        //Step 2: return response
        return ResponseEntity.ok(Map.of("message","Resume deleted successfully"));


    }



}
