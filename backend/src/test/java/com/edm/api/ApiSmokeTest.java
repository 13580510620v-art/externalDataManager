package com.edm.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSmokeTest {

    @Test
    void controllersOnlyUseGetAndPostMappings() throws IOException {
        List<String> controllers = controllerSources().map(this::readFile).toList();

        assertThat(controllers).isNotEmpty();
        assertThat(controllers).allSatisfy(source -> {
            assertThat(source).doesNotContain("@PutMapping");
            assertThat(source).doesNotContain("@PatchMapping");
            assertThat(source).doesNotContain("@DeleteMapping");
        });
    }

    @Test
    void managementEndpointsUseAgreedPathsAndMethods() throws IOException {
        Set<String> mappings = controllerSources()
                .map(this::readFile)
                .flatMap(source -> extractMappings(source).stream())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        assertThat(mappings).containsExactlyInAnyOrder(
                "GET:/api/auth/saml/enabled",
                "GET:/api/auth/me",
                "POST:/api/auth/login",
                "POST:/api/auth/logout",
                "GET:/api/suppliers",
                "POST:/api/suppliers",
                "POST:/api/suppliers/{id}/update",
                "POST:/api/suppliers/{id}/enable",
                "POST:/api/suppliers/{id}/disable",
                "GET:/api/tasks",
                "GET:/api/tasks/{id}",
                "POST:/api/tasks/{id}/retry",
                "GET:/api/dashboard/today",
                "GET:/api/users",
                "POST:/api/users",
                "POST:/api/users/{id}/update",
                "POST:/api/users/{id}/enable",
                "POST:/api/users/{id}/disable",
                "GET:/api/groups",
                "POST:/api/groups",
                "POST:/api/groups/{id}/update",
                "POST:/api/groups/{id}/permissions",
                "GET:/api/permissions",
                "GET:/api/audit-logs"
        );
    }

    private Stream<Path> controllerSources() throws IOException {
        return Files.walk(Path.of("src/main/java/com/edm"))
                .filter(path -> path.toString().endsWith("Controller.java"));
    }

    private List<String> extractMappings(String source) {
        Matcher baseMatcher = Pattern.compile("@RequestMapping\\(\"([^\"]+)\"\\)").matcher(source);
        assertThat(baseMatcher.find()).isTrue();
        String base = baseMatcher.group(1);
        return Pattern.compile("@(Get|Post)Mapping(?:\\(\"([^\"]+)\"\\))?")
                .matcher(source)
                .results()
                .map(result -> result.group(1).toUpperCase() + ":" + base + (result.group(2) == null ? "" : result.group(2)))
                .toList();
    }

    private String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
