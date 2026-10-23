package com.ums.transport.application.impl;

import com.ums.transport.application.TransportRouteService;
import com.ums.transport.infrastructure.TransportRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransportRouteServiceImpl implements TransportRouteService {

    private final TransportRouteRepository transportRouteRepository;
}
