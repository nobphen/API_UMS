package com.ums.admission.formsale.api;

import com.ums.admission.formsale.application.ApplicationFormSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/application-form-sales")
@RequiredArgsConstructor
public class ApplicationFormSaleController {

    private final ApplicationFormSaleService applicationFormSaleService;
}
