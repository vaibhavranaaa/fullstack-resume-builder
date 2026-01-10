package in.vaibhavrana.resumebuilderapi.service;

import in.vaibhavrana.resumebuilderapi.document.Resume;
import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import in.vaibhavrana.resumebuilderapi.dto.CreateResumeRequest;
import in.vaibhavrana.resumebuilderapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AuthService authService;
    public Resume createResume(CreateResumeRequest request, Object principalObject) {
        //Step1: Create resume object
        Resume newResume=new Resume();


        //Step2: Get the current profile
        AuthResponse response=authService.getProfile(principalObject);


        //Step3: update the resume object
        newResume.setUserId(response.getId());
        newResume.setTitle(request.getTitle());

        //Step4: Set default data for resume
        setDefaultResumeData(newResume);


        //Step5: save the resume data
        return resumeRepository.save(newResume);

    }

    private void setDefaultResumeData(Resume newResume) {
        newResume.setProfileInfo(new Resume.ProfileInfo());
        newResume.setContactInfo(new Resume.ContactInfo());
        newResume.setWorkExperience(new ArrayList<>());
        newResume.setEducation(new ArrayList<>());
        newResume.setSkill(new ArrayList<>());
        newResume.setProject(new ArrayList<>());
        newResume.setCertification(new ArrayList<>());
        newResume.setLanguage(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());
    }

    public List<Resume> getUserResumes(Object principal) {
        //Step1: Get the current profile
        AuthResponse response=authService.getProfile(principal);

        //Step2: Call the repository finder method
        List<Resume> resumes =resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

        //Step3: return response
        return resumes;
    }

    public Resume getResumeById(String resumeId, Object principal) {
        //Step1: Get the current profile
        AuthResponse response=authService.getProfile(principal);

        //Step2: Call the repo finder method
        Resume existingResume=resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found"));

        //Step3: return result
        return existingResume;

    }

    public Resume updateResume(String resumeId, Resume updatedData, Object principal) {
        //Step 1: get the current profile
        AuthResponse response=authService.getProfile(principal);

        //Step 2: call the repository
        Resume existingResume=resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found"));

        //Step 3: update the new data
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperience(updatedData.getWorkExperience());
        existingResume.setEducation(updatedData.getEducation());
        existingResume.setSkill(updatedData.getSkill());
        existingResume.setProject(updatedData.getProject());
        existingResume.setCertification(updatedData.getCertification());
        existingResume.setLanguage(updatedData.getLanguage());
        existingResume.setInterests(updatedData.getInterests());

        //Step4: update the details into database
        resumeRepository.save(existingResume);

        //Step5: return result;
        return existingResume;
    }

    public void deleteResume(String resumeId, Object principal) {
        //Step1: get the currentProfile
        AuthResponse response=authService.getProfile(principal);

        //Step2: call the rep finder method
        Resume existingResume=resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(()-> new RuntimeException("Resume not found"));

        resumeRepository.delete(existingResume);
    }
}
