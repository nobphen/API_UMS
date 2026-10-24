package com.ums.graduation.api;

import com.ums.graduation.application.ThesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/theses")
@RequiredArgsConstructor
public class ThesisController {

    private final ThesisService thesisService;
}
