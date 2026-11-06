package com.ums.security.session.application.impl;

import com.ums.security.session.application.GeoIpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Not written yet. Later: looks up the IP in the GeoLite2 database and returns country and city. */
@Service
@Transactional
@RequiredArgsConstructor
public class GeoIpServiceImpl implements GeoIpService {
}
