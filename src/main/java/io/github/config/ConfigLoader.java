package io.github.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class ConfigLoader {

    private ConfigLoader() {
    }

    public static GeneratorConfig load() {

        Path properties = Paths.get("src/main/resources/application.properties");
        Path yml = Paths.get("src/main/resources/application.yml");
        Path yaml = Paths.get("src/main/resources/application.yaml");

        try {

            if (Files.exists(properties)) {
                return loadFromProperties(properties);
            }

            if (Files.exists(yml)) {
                return loadFromYaml(yml);
            }

            if (Files.exists(yaml)) {
                return loadFromYaml(yaml);
            }

            throw new RuntimeException(
                    "application.properties / application.yml / application.yaml tidak ditemukan.");

        } catch (Exception e) {
            throw new RuntimeException("Gagal membaca konfigurasi.", e);
        }

    }

    private static GeneratorConfig loadFromProperties(Path path)
            throws Exception {

        Properties prop = new Properties();

        try (InputStream in = Files.newInputStream(path)) {
            prop.load(in);
        }

        GeneratorConfig config = new GeneratorConfig();

        config.setBasePackage(require(prop, "generator.base-package"));

        config.setEntityDir(
                Paths.get(require(prop, "generator.entity-dir")));

        config.setRepoDir(
                Paths.get(require(prop, "generator.repo-dir")));

        config.setDatabase(
                require(prop, "generator.hibernate.database"));

        config.setOutputReveng(
                prop.getProperty(
                        "generator.hibernate.output",
                        "hibernate.reveng.xml"));

        String tablesStr = require(prop, "generator.hibernate.tables");
        config.setTables(java.util.Arrays.stream(tablesStr.split(","))
                .map(String::trim)
                .toList());

        config.setDbUrl(require(prop, "spring.datasource.url"));
        config.setDbUser(require(prop, "spring.datasource.username"));
        config.setDbPassword(require(prop, "spring.datasource.password"));
        config.setDbDriver(prop.getProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver"));

        return config;
    }

    private static GeneratorConfig loadFromYaml(Path path)
            throws Exception {

        YAMLMapper mapper = new YAMLMapper();

        JsonNode root;

        try (InputStream in = Files.newInputStream(path)) {
            root = mapper.readTree(in);
        }

        GeneratorConfig config = new GeneratorConfig();

        config.setBasePackage(
                require(root, "generator", "base-package"));

        config.setEntityDir(
                Paths.get(
                        require(root,
                                "generator",
                                "entity-dir")));

        config.setRepoDir(
                Paths.get(
                        require(root,
                                "generator",
                                "repo-dir")));

        config.setDatabase(
                require(root,
                        "generator",
                        "hibernate",
                        "database"));

        JsonNode output = root.path("generator")
                .path("hibernate")
                .path("output");

        config.setOutputReveng(
                output.isMissingNode()
                        ? "hibernate.reveng.xml"
                        : output.asText());

        JsonNode tablesNode = root.path("generator").path("hibernate").path("tables");
        if (tablesNode.isMissingNode()) {
            throw new IllegalArgumentException("Konfigurasi 'generator.hibernate.tables' wajib diisi.");
        }
        java.util.List<String> tables = new java.util.ArrayList<>();
        if (tablesNode.isArray()) {
            for (JsonNode node : tablesNode) {
                tables.add(node.asText().trim());
            }
        } else {
            java.util.Arrays.stream(tablesNode.asText().split(","))
                    .map(String::trim)
                    .forEach(tables::add);
        }
        config.setTables(tables);

        config.setDbUrl(require(root, "spring", "datasource", "url"));
        config.setDbUser(require(root, "spring", "datasource", "username"));
        config.setDbPassword(require(root, "spring", "datasource", "password"));
        
        JsonNode driverNode = root.path("spring").path("datasource").path("driver-class-name");
        config.setDbDriver(driverNode.isMissingNode() ? "com.mysql.cj.jdbc.Driver" : driverNode.asText().trim());

        return config;

    }

    private static String require(Properties prop,
                                  String key) {

        String value = prop.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Property '" + key + "' wajib diisi.");
        }

        return value.trim();
    }

    private static String require(JsonNode root,
                                  String... paths) {

        JsonNode node = root;

        for (String p : paths) {
            node = node.path(p);
        }

        if (node.isMissingNode() || node.asText().isBlank()) {

            throw new IllegalArgumentException(
                    "Konfigurasi '" +
                            String.join(".", paths)
                            + "' wajib diisi.");

        }

        return node.asText().trim();

    }

}