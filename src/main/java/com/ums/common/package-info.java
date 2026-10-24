/**
 * Shared code every module may use: base entities, the API response, exceptions and {@code @RequirePermission}.
 * OPEN means all of its packages are public to the other modules.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package com.ums.common;

import org.springframework.modulith.ApplicationModule;
