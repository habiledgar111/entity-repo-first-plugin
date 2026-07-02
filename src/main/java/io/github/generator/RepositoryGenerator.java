package io.github.generator; // Disesuaikan ke package generator proyek Anda

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import io.github.config.GeneratorConfig;

public class RepositoryGenerator {

    public static void generate(GeneratorConfig config) throws Exception {

        if (config == null) {
            System.out.println("Config is null!");
            return;
        }

        if (!Files.exists(config.getEntityDir())) {
            System.out.println("Folder Entity tidak ditemukan di: " + config.getEntityDir().toAbsolutePath());
            return;
        }

        Files.createDirectories(config.getRepoDir());

        Files.walk(config.getEntityDir())
                .filter(f -> f.toString().endsWith(".java"))
                // Mengabaikan kelas ID komposit (seperti OrderItemsId) agar tidak dibuatkan
                // repo terpisah
                .filter(f -> !f.getFileName().toString().endsWith("Id.java"))
                .forEach(f -> generateRepository(f, config));

        System.out.println("Done.");
    }

    private static void generateRepository(Path entityFile, GeneratorConfig config) {

        try {
            CompilationUnit cu = StaticJavaParser.parse(entityFile);
            Optional<ClassOrInterfaceDeclaration> clazzOpt = cu.findFirst(ClassOrInterfaceDeclaration.class);

            if (clazzOpt.isEmpty()) {
                return;
            }

            ClassOrInterfaceDeclaration clazz = clazzOpt.get();

            // Skip kalau bukan Entity
            boolean isEntity = clazz.getAnnotations()
                    .stream()
                    .anyMatch(a -> a.getNameAsString().equals("Entity"));

            if (!isEntity) {
                return;
            }

            String entityName = clazz.getNameAsString();
            String idType = toWrapperType(resolveIdType(clazz));

            Path repoFile = config.getRepoDir().resolve(entityName + "Repository.java");

            if (Files.exists(repoFile)) {
                System.out.println(entityName + "Repository already exists.");
                return;
            }

            // Memperbaiki deteksi import untuk ID Komposit (Many-to-Many jembatan)
            String idImport = "";
            if (!isJavaLangType(idType)) {
                idImport = "import " + config.getBasePackage() + ".entity." + idType + ";\n";
            }

            String source = "package %s.repository;\n\n" +
                    "import %s.entity.%s;\n" +
                    "%s\n" +
                    "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                    "import org.springframework.stereotype.Repository;\n\n" +
                    "@Repository\n" +
                    "public interface %sRepository extends JpaRepository<%s, %s> {\n\n" +
                    "}\n";
            source = String.format(source,
                    config.getBasePackage(),
                    config.getBasePackage(),
                    entityName,
                    idImport,
                    entityName,
                    entityName,
                    idType);

            Files.writeString(repoFile, source);
            System.out.println("Generated : " + entityName + "Repository");

        } catch (Exception e) {
            System.out.println("Skip : " + entityFile.getFileName());
            e.printStackTrace();
        }
    }

    private static boolean isJavaLangType(String type) {
        return switch (type) {
            case "Long",
                    "Integer",
                    "Short",
                    "Byte",
                    "Double",
                    "Float",
                    "Boolean",
                    "Character",
                    "String" ->
                true;
            default -> false;
        };
    }

    private static String resolveIdType(ClassOrInterfaceDeclaration clazz) {
        // Field Access
        for (FieldDeclaration field : clazz.getFields()) {
            boolean isId = field.getAnnotations()
                    .stream()
                    .anyMatch(a -> a.getNameAsString().equals("Id")
                            || a.getNameAsString().equals("EmbeddedId"));

            if (isId) {
                return field.getVariable(0).getType().asString();
            }
        }

        // Property Access
        for (MethodDeclaration method : clazz.getMethods()) {
            boolean isId = method.getAnnotations()
                    .stream()
                    .anyMatch(a -> a.getNameAsString().equals("Id")
                            || a.getNameAsString().equals("EmbeddedId"));

            if (isId) {
                return method.getType().asString();
            }
        }

        throw new RuntimeException("Primary key tidak ditemukan pada entity : " + clazz.getNameAsString());
    }

    private static String toWrapperType(String type) {
        return switch (type) {
            case "byte" -> "Byte";
            case "short" -> "Short";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "boolean" -> "Boolean";
            case "char" -> "Character";
            default -> type;
        };
    }
}