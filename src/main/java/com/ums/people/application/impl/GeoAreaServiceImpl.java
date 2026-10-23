package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.GeoAreaRequest;
import com.ums.people.api.dto.GeoAreaResponse;
import com.ums.people.api.dto.GeoAreaUpdateRequest;
import com.ums.people.application.GeoAreaService;
import com.ums.people.application.mapper.GeoAreaMapper;
import com.ums.people.domain.GeoArea;
import com.ums.people.infrastructure.GeoAreaRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GeoAreaServiceImpl implements GeoAreaService {

    private final GeoAreaRepository geoAreaRepository;
    private final GeoAreaMapper geoAreaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GeoAreaResponse> list(Long parentId, String q, boolean includeInactive) {
        String search = q == null ? "" : q.trim();   // "" matches every name
        return geoAreaRepository.findForList(parentId, search, includeInactive).stream()
                .map(geoAreaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoAreaResponse get(Long id) {
        return geoAreaMapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeoAreaResponse> path(Long id) {
        List<GeoAreaResponse> path = new ArrayList<>();
        for (GeoArea area = find(id); area != null; area = area.getParent()) {
            path.add(geoAreaMapper.toResponse(area));
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public GeoAreaResponse create(GeoAreaRequest request) {
        GeoArea.Level level = request.level();
        GeoArea parent = null;
        if (level == GeoArea.Level.PROVINCE) {
            if (request.parentId() != null) {
                throw new BusinessException("A province has no parent");
            }
        } else {
            if (request.parentId() == null) {
                throw new BusinessException("A " + level.name().toLowerCase() + " needs its parent " + level.parentLevel().name().toLowerCase());
            }
            parent = find(request.parentId());
            if (parent.getLevel() != level.parentLevel()) {
                throw new BusinessException("A " + level.name().toLowerCase() + " must be inside a " + level.parentLevel().name().toLowerCase());
            }
        }

        // Official codes: 2 digits per level, each starting with the parent's code
        int expectedLength = (level.ordinal() + 1) * 2;
        if (request.code().length() != expectedLength) {
            throw new BusinessException("A " + level.name().toLowerCase() + " code has " + expectedLength + " digits");
        }
        if (parent != null && !request.code().startsWith(parent.getCode())) {
            throw new BusinessException("The code must start with the parent's code " + parent.getCode());
        }
        if (geoAreaRepository.existsByCode(request.code())) {
            throw new BusinessException("Code " + request.code() + " is already used");
        }

        GeoArea area = new GeoArea();
        area.setCode(request.code());
        area.setLevel(level);
        area.setParent(parent);
        area.setNameKh(request.nameKh().trim());
        area.setNameEn(request.nameEn().trim());
        area.setUnitKh(request.unitKh());
        area.setUnitEn(request.unitEn());
        geoAreaRepository.save(area);
        return geoAreaMapper.toResponse(area);
    }

    @Override
    public GeoAreaResponse update(Long id, GeoAreaUpdateRequest request) {
        GeoArea area = find(id);
        area.setNameKh(request.nameKh().trim());
        area.setNameEn(request.nameEn().trim());
        area.setUnitKh(request.unitKh());
        area.setUnitEn(request.unitEn());
        if (request.active() != null) {
            area.setActive(request.active());
        }
        return geoAreaMapper.toResponse(area);
    }

    private GeoArea find(Long id) {
        return geoAreaRepository.findById(id).orElseThrow(() -> new NotFoundException("Area not found"));
    }
}
