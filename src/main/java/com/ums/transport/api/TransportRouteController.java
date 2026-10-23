package com.ums.transport.api;

import com.ums.transport.application.TransportRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transport-routes")
@RequiredArgsConstructor
public class TransportRouteController {

    private final TransportRouteService transportRouteService;
}
