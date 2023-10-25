package t.me.p1azmer.plugin.last_server.dependencies;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public enum BaseLibrary {
    H2_V1(
            "com.h2database",
            "h2",
            "1.4.200"
    ),
    H2_V2(
            "com.h2database",
            "h2",
            "2.1.214"
    ),
    MYSQL(
            "com.mysql",
            "mysql-connector-j",
            "8.0.33"
    ),
    MARIADB(
            "org.mariadb.jdbc",
            "mariadb-java-client",
            "3.1.4"
    ),
    POSTGRESQL(
            "org.postgresql",
            "postgresql",
            "42.5.1"
    ),
    SQLITE(
            "org.xerial",
            "sqlite-jdbc",
            "3.40.0.0"
    );

    private final Path filenamePath;
    private final URL mavenRepoURL;

    BaseLibrary(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        String mavenPath = String.format("%s/%s/%s/%s-%s.jar",
                groupId.replace(".", "/"),
                artifactId,
                version,
                artifactId,
                version
        );

        this.filenamePath = Path.of("libraries/" + mavenPath);

        try {
            this.mavenRepoURL = new URL("https://repo1.maven.org/maven2/" + mavenPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @NotNull
    public URL getClassLoaderURL() throws MalformedURLException {
        if (!Files.exists(this.filenamePath)) {
            try {
                try (InputStream in = this.mavenRepoURL.openStream()) {
                    Files.createDirectories(this.filenamePath.getParent());
                    Files.copy(in, Files.createFile(this.filenamePath), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return this.filenamePath.toUri().toURL();
    }
}