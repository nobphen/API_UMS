package com.ums.graduation.api;

import com.ums.graduation.application.TranscriptRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transcript-requests")
@RequiredArgsConstructor
public class TranscriptRequestController {

    private final TranscriptRequestService transcriptRequestService;
}
