package com.ums;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(UmsApplication.class);

    @Test
    void listsModules() {
        modules.forEach(System.out::println);
    }

    @Test
    void writesModuleDocs() {
        new Documenter(modules).writeModulesAsPlantUml();
    }
}
