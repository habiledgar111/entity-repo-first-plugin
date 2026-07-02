package io.github.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RevengGenerator {

    public static void generate(io.github.config.GeneratorConfig config,
            Path outputFile) throws IOException {

        StringBuilder tableFilters = new StringBuilder();
        for (String table : config.getTables()) {
            tableFilters.append(String.format("    <table-filter match-name=\"%s\"/>\n", table));
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE hibernate-reverse-engineering PUBLIC\n" +
                "\"-//Hibernate/Hibernate Reverse Engineering DTD 3.0//EN\"\n" +
                "\"http://hibernate.org/dtd/hibernate-reverse-engineering-3.0.dtd\">\n" +
                "\n" +
                "<hibernate-reverse-engineering>\n" +
                "\n" +
                "    <schema-selection match-catalog=\"%s\"/>\n" +
                "\n" +
                "%s\n" +
                "    <table-filter match-name=\".*\" exclude=\"true\"/>\n" +
                "\n" +
                "</hibernate-reverse-engineering>\n";
        xml = String.format(xml, config.getDatabase(), tableFilters.toString());

        Files.writeString(outputFile, xml);
    }

}