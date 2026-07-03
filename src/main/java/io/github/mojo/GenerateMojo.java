package io.github.mojo;

import java.io.File;
import java.util.Collections;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;

import io.github.config.ConfigLoader;
import io.github.config.GeneratorConfig;
import io.github.generator.HibernatePropertiesGenerator;
import io.github.generator.RepositoryGenerator;
import io.github.generator.RevengGenerator;

@Mojo(name = "run")
public class GenerateMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Memulai proses generator via Maven Plugin...");

            // 1. Load config
            GeneratorConfig config = ConfigLoader.load();
            getLog().info("Database: " + config.getDatabase());
            getLog().info("Tables: " + String.join(", ", config.getTables()));

            // 2. Generate hibernate.reveng.xml
            File revengFile = new File("src/main/resources/" + config.getOutputReveng());
            RevengGenerator.generate(config, revengFile.toPath());
            getLog().info("hibernate.reveng.xml berhasil dibuat.");

            // 3. Auto-generate hibernate.properties dari config
            File hibernateProps = new File("hibernate.properties");
            HibernatePropertiesGenerator.generate(config, hibernateProps.toPath());
            getLog().info("hibernate.properties berhasil dibuat otomatis.");

            // 4. Buat file hibernate-generator-pom.xml sementara
            File tempPom = new File("hibernate-generator-pom.xml");
            String safeDbUrl = config.getDbUrl().replace("&", "&amp;");

            String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "    <modelVersion>4.0.0</modelVersion>\n" +
                    "    <groupId>io.github.generator</groupId>\n" +
                    "    <artifactId>temp-hibernate-generator</artifactId>\n" +
                    "    <version>1.0.0</version>\n" +
                    "    <build>\n" +
                    "        <plugins>\n" +
                    "            <plugin>\n" +
                    "                <groupId>org.hibernate.tool</groupId>\n" +
                    "                <artifactId>hibernate-tools-maven</artifactId>\n" +
                    "                <version>6.5.2.Final</version>\n" +
                    "                <configuration>\n" +
                    "                    <ejb3>true</ejb3>\n" +
                    "                    <outputDirectory>${project.basedir}/src/main/java</outputDirectory>\n" +
                    "                    <packageName>" + config.getBasePackage() + ".entity</packageName>\n" +
                    "                    <revengFile>${project.basedir}/src/main/resources/" + config.getOutputReveng() + "</revengFile>\n" +
                    "                    <properties>\n" +
                    "                        <jakarta.persistence.jdbc.url>" + safeDbUrl + "</jakarta.persistence.jdbc.url>\n" +
                    "                        <jakarta.persistence.jdbc.user>" + config.getDbUser() + "</jakarta.persistence.jdbc.user>\n" +
                    "                        <jakarta.persistence.jdbc.password>" + config.getDbPassword() + "</jakarta.persistence.jdbc.password>\n" +
                    "                        <jakarta.persistence.jdbc.driver>" + config.getDbDriver() + "</jakarta.persistence.jdbc.driver>\n" +
                    "                    </properties>\n" +
                    "                </configuration>\n" +
                    "                <dependencies>\n" +
                    "                    <dependency>\n" +
                    "                        <groupId>com.mysql</groupId>\n" +
                    "                        <artifactId>mysql-connector-j</artifactId>\n" +
                    "                        <version>9.0.0</version>\n" +
                    "                    </dependency>\n" +
                    "                </dependencies>\n" +
                    "            </plugin>\n" +
                    "        </plugins>\n" +
                    "    </build>\n" +
                    "</project>";

            java.nio.file.Files.writeString(tempPom.toPath(), pomContent);
            getLog().info("File POM sementara berhasil dibuat.");

            // 5. Jalankan maven hibernate-tools
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(tempPom);
            // Set working directory agar hibernate.properties ditemukan
            request.setBaseDirectory(new File(System.getProperty("user.dir")));
            // Memanggil goal spesifik hibernate-tools-maven
            request.setGoals(Collections.singletonList("org.hibernate.tool:hibernate-tools-maven:6.5.2.Final:hbm2java"));
            request.setBatchMode(true);

            Invoker invoker = new DefaultInvoker();
            String mavenHome = System.getenv("M2_HOME");
            if (mavenHome == null) {
                mavenHome = System.getenv("MAVEN_HOME");
            }
            if (mavenHome != null) {
                invoker.setMavenHome(new File(mavenHome));
            }

            getLog().info("Menjalankan hibernate-tools via Maven Invoker...");
            InvocationResult result = invoker.execute(request);

            // Hapus file-file sementara setelah eksekusi selesai (berhasil atau gagal)
            if (tempPom.exists()) {
                tempPom.delete();
            }
            if (hibernateProps.exists()) {
                hibernateProps.delete();
                getLog().info("hibernate.properties sementara berhasil dihapus.");
            }

            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("Gagal menjalankan hibernate-tools:hbm2java", result.getExecutionException());
            }
            getLog().info("Berhasil generate Entity.");

            // 6. Generate Repository
            getLog().info("Memulai generate Repository...");
            RepositoryGenerator.generate(config);

            getLog().info("Seluruh proses pembuatan entity & repository selesai.");

        } catch (Exception e) {
            throw new MojoExecutionException("Error selama proses generate", e);
        }
    }
}
