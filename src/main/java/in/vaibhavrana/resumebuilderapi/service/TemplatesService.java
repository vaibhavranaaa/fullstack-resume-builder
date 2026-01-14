package in.vaibhavrana.resumebuilderapi.service;

import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static in.vaibhavrana.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplatesService {

    private final AuthService authService;

    public Map<String, Object> getTemplates(Object principal){

        //Step1:get the current profile
        AuthResponse authResponse=authService.getProfile(principal);

        //Step2: get the available templates based on subscription
        List<String> availableTemplates;

        Boolean isPremium=PREMIUM.equalsIgnoreCase(authResponse.getSubscriptionPlan());

        if(isPremium){
            availableTemplates=List.of("01","02","03");
        }else{
            availableTemplates=List.of("01");
        }

        //Step3: Add the data into map
        Map<String,Object> restriction=new HashMap<>();
        restriction.put("availableTemplates",availableTemplates);
        restriction.put("allTemplates",List.of("01","02","03"));
        restriction.put("subscriptionPlan",authResponse.getSubscriptionPlan());
        restriction.put("isPremium",isPremium);


        //Step 4: return the result;
        return restriction;

    }
}

