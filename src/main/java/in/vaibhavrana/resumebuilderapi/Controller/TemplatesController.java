package in.vaibhavrana.resumebuilderapi.Controller;


import in.vaibhavrana.resumebuilderapi.service.TemplatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/templates")
@RestController
@Slf4j

public class TemplatesController {
    private final TemplatesService templatesService;
    @GetMapping
    public ResponseEntity<?> getTemplates(Authentication authentication){
        //Step1: Call the service method
        Map<String,Object>response=templatesService.getTemplates(authentication.getPrincipal());

        //Step2: Return the response
        return ResponseEntity.ok(response);

    }
}
