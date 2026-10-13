package com.personaleconomy.config;

import com.personaleconomy.service.impl.MysqlWorkspaceService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Runs after Flyway; any failure aborts startup rather than silently switching storage. */
@Component
@Profile("mysql & !bootstrap & !prod")
public class MysqlWorkspaceInitializer implements ApplicationRunner {
    private final MysqlWorkspaceService workspace;
    public MysqlWorkspaceInitializer(MysqlWorkspaceService workspace) { this.workspace = workspace; }
    @Override public void run(ApplicationArguments args) { workspace.initialize(); }
}
