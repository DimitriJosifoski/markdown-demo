package com.steelworks.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SteelworksTrackerApplication — the entry point for the entire Spring Boot application.
 *
 * <p><b>What {@code @SpringBootApplication} does (three annotations in one):</b></p>
 * <ul>
 *   <li>{@code @Configuration}    – marks this class as a source of bean definitions.</li>
 *   <li>{@code @EnableAutoConfiguration} – tells Spring Boot to guess configuration
 *       based on the JARs on the classpath (e.g., seeing H2 → auto-configure an in-memory DB).</li>
 *   <li>{@code @ComponentScan}    – scans the {@code com.steelworks.tracker} package
 *       (and sub-packages) for {@code @Component}, {@code @Service}, {@code @Repository},
 *       and {@code @Controller} classes so Spring can manage them.</li>
 * </ul>
 *
 * <p><b>How to run:</b></p>
 * <pre>
 *   mvn spring-boot:run
 * </pre>
 * or build a fat JAR and execute it:
 * <pre>
 *   mvn clean package
 *   java -jar target/steelworks-tracker-0.0.1-SNAPSHOT.jar
 * </pre>
 */
@SpringBootApplication
public class SteelworksTrackerApplication {

    /**
     * JVM entry point.  Delegates immediately to Spring Boot's {@link SpringApplication#run}
     * which:
     * <ol>
     *   <li>Creates the Spring IoC container (ApplicationContext).</li>
     *   <li>Starts the embedded Tomcat server.</li>
     *   <li>Initialises Hibernate / JPA and connects to the database.</li>
     * </ol>
     *
     * @param args command-line arguments (Spring Boot parses these for overrides,
     *             e.g. {@code --server.port=9090}).
     */
    public static void main(String[] args) {
        SpringApplication.run(SteelworksTrackerApplication.class, args);
    }
}
