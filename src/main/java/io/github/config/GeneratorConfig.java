package io.github.config;

import java.nio.file.Path;

public class GeneratorConfig {

    private String basePackage;
    private Path entityDir;
    private Path repoDir;

    private String database;
    private String outputReveng;
    private java.util.List<String> tables;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbDriver;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Path getEntityDir() {
        return entityDir;
    }

    public void setEntityDir(Path entityDir) {
        this.entityDir = entityDir;
    }

    public Path getRepoDir() {
        return repoDir;
    }

    public void setRepoDir(Path repoDir) {
        this.repoDir = repoDir;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getOutputReveng() {
        return outputReveng;
    }

    public void setOutputReveng(String outputReveng) {
        this.outputReveng = outputReveng;
    }

    public java.util.List<String> getTables() {
        return tables;
    }

    public void setTables(java.util.List<String> tables) {
        this.tables = tables;
    }

    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }

    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }

    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }

    public String getDbDriver() { return dbDriver; }
    public void setDbDriver(String dbDriver) { this.dbDriver = dbDriver; }
}