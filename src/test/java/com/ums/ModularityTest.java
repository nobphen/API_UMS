package com.ums;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(UmsApplication.class);

    /** Fails when a module uses another module's internal classes or two modules depend on each other in a cycle. */
    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }

    @Test
    void listsModules() {
        modules.forEach(System.out::println);
    }

    @Test
    void writesModuleDocs() {
        new Documenter(modules).writeModulesAsPlantUml();
    }
}
