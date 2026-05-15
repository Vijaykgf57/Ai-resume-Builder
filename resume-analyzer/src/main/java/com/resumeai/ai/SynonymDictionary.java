package com.resumeai.ai;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Synonym dictionary for intelligent skill matching.
 * Maps skill variants/synonyms to a canonical skill name.
 * This enables "Spring Boot" to match a job requiring "Spring", etc.
 */
@Component
public class SynonymDictionary {

    // Maps any variant → canonical skill name
    private final Map<String, String> synonymMap = new HashMap<>();

    public SynonymDictionary() {
        // Java ecosystem
        register("java", "Java", "java", "core java", "java se", "java ee", "j2ee", "j2se");
        register("Spring", "Spring", "spring", "spring framework", "spring boot", "springboot", "spring mvc", "spring core");
        register("Hibernate", "Hibernate", "hibernate", "jpa", "java persistence api", "spring data jpa");
        register("Maven", "Maven", "maven", "apache maven");
        register("Gradle", "Gradle", "gradle");

        // Web / API
        register("REST API", "REST API", "rest", "rest api", "restful", "restful api", "web services",
                "web service", "http api", "json api", "rest services");
        register("GraphQL", "GraphQL", "graphql");
        register("SOAP", "SOAP", "soap", "soap web services");

        // JavaScript ecosystem
        register("JavaScript", "JavaScript", "javascript", "js", "es6", "es2015", "ecmascript");
        register("TypeScript", "TypeScript", "typescript", "ts");
        register("React", "React", "react", "reactjs", "react.js");
        register("Angular", "Angular", "angular", "angularjs", "angular.js");
        register("Node.js", "Node.js", "node", "nodejs", "node.js");

        // Python
        register("Python", "Python", "python", "python3", "python 3");
        register("Django", "Django", "django");
        register("Flask", "Flask", "flask");

        // Databases
        register("SQL", "SQL", "sql", "structured query language");
        register("MySQL", "MySQL", "mysql");
        register("PostgreSQL", "PostgreSQL", "postgresql", "postgres");
        register("MongoDB", "MongoDB", "mongodb", "mongo");
        register("Redis", "Redis", "redis");
        register("Oracle", "Oracle", "oracle", "oracle db", "oracle database");

        // Cloud & DevOps
        register("AWS", "AWS", "aws", "amazon web services", "amazon aws");
        register("Docker", "Docker", "docker", "docker container", "containerization");
        register("Kubernetes", "Kubernetes", "kubernetes", "k8s");
        register("CI/CD", "CI/CD", "ci/cd", "cicd", "continuous integration", "continuous deployment",
                "jenkins", "github actions", "gitlab ci");
        register("Git", "Git", "git", "github", "gitlab", "bitbucket", "version control");

        // Microservices
        register("Microservices", "Microservices", "microservices", "microservice", "micro services",
                "micro-services", "service oriented architecture", "soa");

        // Testing
        register("JUnit", "JUnit", "junit", "junit4", "junit5", "unit testing", "unit test");
        register("Mockito", "Mockito", "mockito", "mocking");

        // Other
        register("Linux", "Linux", "linux", "unix", "bash", "shell scripting");
        register("Agile", "Agile", "agile", "scrum", "kanban", "agile methodology");
    }

    /**
     * Registers all variants pointing to the canonical name.
     */
    private void register(String canonical, String... variants) {
        for (String variant : variants) {
            synonymMap.put(variant.toLowerCase().trim(), canonical);
        }
    }

    /**
     * Resolves a raw skill string to its canonical form.
     * Returns the input as-is (title-cased) if no mapping found.
     */
    public String resolve(String rawSkill) {
        if (rawSkill == null) return null;
        String key = rawSkill.toLowerCase().trim();
        return synonymMap.getOrDefault(key, toTitleCase(rawSkill.trim()));
    }

    /**
     * Checks if two skill names are equivalent (same canonical form).
     */
    public boolean areEquivalent(String skill1, String skill2) {
        return resolve(skill1).equalsIgnoreCase(resolve(skill2));
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    public Map<String, String> getSynonymMap() {
        return Collections.unmodifiableMap(synonymMap);
    }
}
