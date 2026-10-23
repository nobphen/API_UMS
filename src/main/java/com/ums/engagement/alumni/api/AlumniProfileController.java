package com.ums.engagement.alumni.api;

import com.ums.engagement.alumni.application.AlumniProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alumni-profiles")
@RequiredArgsConstructor
public class AlumniProfileController {

    private final AlumniProfileService alumniProfileService;
}
