package com.bustrack.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Dynamically configures the DataSource to ensure smooth deployment on Render, Heroku, or local.
 * - If Render/Heroku DATABASE_URL or DB_URL is provided, parses and connects to PostgreSQL.
 * - If no external database is configured (or points to unconfigured localhost:5432),
 *   seamlessly falls back to an embedded H2 database (in PostgreSQL mode) so the app runs out-of-the-box.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:#{null}}")
    private String propUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String propUsername;

    @Value("${spring.datasource.password:#{null}}")
    private String propPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("DB_URL");
        String databaseUrl = System.getenv("DATABASE_URL");
        String url = (dbUrl != null && !dbUrl.isBlank()) ? dbUrl.trim() :
                     (databaseUrl != null && !databaseUrl.isBlank()) ? databaseUrl.trim() :
                     (propUrl != null && !propUrl.isBlank()) ? propUrl.trim() : null;

        // If no external URL or pointing to non-existent localhost:5432 on cloud
        boolean isUnconfiguredLocalhost = url != null && url.contains("localhost:5432")
                && (System.getenv("RENDER") != null || System.getenv("PORT") != null || System.getenv("DYNO") != null);
        boolean isH2Url = url != null && url.startsWith("jdbc:h2:");
        boolean noUrl = (url == null || url.isBlank());

        if (noUrl || isUnconfiguredLocalhost || isH2Url) {
            log.info("Using embedded H2 database (PostgreSQL compatibility mode) for zero-config operation.");
            try {
                java.io.File dataDir = new java.io.File("./data");
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }
            } catch (Exception e) {
                log.warn("Could not create ./data directory: {}", e.getMessage());
            }
            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl("jdbc:h2:file:./data/bustrack;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setMaximumPoolSize(10);
            return new HikariDataSource(h2Config);
        }

        // Support Render / Heroku postgresql:// or postgres:// URI format
        HikariConfig config = new HikariConfig();
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            try {
                URI uri = new URI(url);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl += "?" + uri.getQuery();
                } else {
                    jdbcUrl += "?sslmode=prefer";
                }

                config.setJdbcUrl(jdbcUrl);
                if (uri.getUserInfo() != null) {
                    String[] parts = uri.getUserInfo().split(":", 2);
                    config.setUsername(parts[0]);
                    if (parts.length > 1) {
                        config.setPassword(parts[1]);
                    }
                }
                config.setDriverClassName("org.postgresql.Driver");
                log.info("Configured PostgreSQL datasource from URI: jdbc:postgresql://{}:{}{}", host, port, path);
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.warn("Could not parse URI '{}', treating as raw JDBC url: {}", url, e.getMessage());
            }
        }

        if (!url.startsWith("jdbc:")) {
            url = "jdbc:" + url;
        }

        config.setJdbcUrl(url);

        String username = (System.getenv("DB_USERNAME") != null && !System.getenv("DB_USERNAME").isBlank())
                ? System.getenv("DB_USERNAME") : propUsername;
        String password = (System.getenv("DB_PASSWORD") != null)
                ? System.getenv("DB_PASSWORD") : propPassword;

        if (username != null && !username.isBlank()) {
            config.setUsername(username);
        }
        if (password != null) {
            config.setPassword(password);
        }

        if (url.contains("postgresql")) {
            config.setDriverClassName("org.postgresql.Driver");
        } else if (url.contains("h2")) {
            config.setDriverClassName("org.h2.Driver");
        }

        log.info("Configured datasource with JDBC URL: {}", url.replaceAll("(?<=://)[^@]+@", "***@"));
        return new HikariDataSource(config);
    }
}
