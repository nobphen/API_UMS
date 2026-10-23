package com.ums.security.session.application.impl;

import com.ums.security.session.application.GeoIpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GeoIpServiceImpl implements GeoIpService {
}
