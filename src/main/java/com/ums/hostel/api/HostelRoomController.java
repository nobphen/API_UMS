package com.ums.hostel.api;

import com.ums.hostel.application.HostelRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hostel-rooms")
@RequiredArgsConstructor
public class HostelRoomController {

    private final HostelRoomService hostelRoomService;
}
