package com.ums.admission.intake.api;

import com.ums.admission.intake.application.AdmissionIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admission-intakes")
@RequiredArgsConstructor
public class AdmissionIntakeController {

    private final AdmissionIntakeService admissionIntakeService;
}
