package com.ums.asset.application.impl;

import com.ums.asset.application.AssetCategoryService;
import com.ums.asset.infrastructure.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AssetCategoryServiceImpl implements AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;
}
