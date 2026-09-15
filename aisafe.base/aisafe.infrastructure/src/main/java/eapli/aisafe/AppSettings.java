package eapli.aisafe;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppSettings.class);

    private static final String PROPERTIES_RESOURCE = "application.properties";
    private static final String CONFIG_FILE_PROPERTY = "aisafe.config";
    private static final String CONFIG_FILE_ENV = "AISAFE_CONFIG";
    private static final String ENV_FILE_ENV = "AISAFE_ENV";

    private static final String JDBC_URL_ENV = "AISAFE_JDBC_URL";
    private static final String JDBC_USER_ENV = "AISAFE_JDBC_USER";
    private static final String JDBC_PASSWORD_ENV = "AISAFE_DB_PASSWORD";
    private static final String JDBC_DRIVER_ENV = "AISAFE_JDBC_DRIVER";

    private static final String JDBC_URL_KEY = "jakarta.persistence.jdbc.url";
    private static final String JDBC_USER_KEY = "jakarta.persistence.jdbc.user";
    private static final String JDBC_PASSWORD_KEY = "jakarta.persistence.jdbc.password";
    private static final String JDBC_DRIVER_KEY = "jakarta.persistence.jdbc.driver";

    private static final List<EnvMapping> ENV_MAPPINGS = List.of(
            new EnvMapping(JDBC_URL_ENV, JDBC_URL_KEY),
            new EnvMapping(JDBC_USER_ENV, JDBC_USER_KEY),
            new EnvMapping(JDBC_PASSWORD_ENV, JDBC_PASSWORD_KEY),
            new EnvMapping(JDBC_DRIVER_ENV, JDBC_DRIVER_KEY)
    );

    private static final String REPOSITORY_FACTORY_KEY = "persistence.repositoryFactory";
    private static final String UI_MENU_LAYOUT_KEY = "ui.menu.layout";
    private static final String PERSISTENCE_UNIT_KEY = "persistence.persistenceUnit";

    private static final String[] EXTENDED_PERSISTENCE_PREFIXES = {
            "jakarta.persistence.jdbc.",
            "jakarta.persistence.schema-generation.",
            "hibernate.dialect",
            "hibernate.connection."
    };

    private final Properties applicationProperties = new Properties();

    public AppSettings() {
        loadProperties();
    }

    private void loadProperties() {
        try (var propertiesStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (propertiesStream == null) {
                throw new FileNotFoundException("property file '" + PROPERTIES_RESOURCE + "' not found in classpath");
            }
            this.applicationProperties.load(propertiesStream);
        } catch (final IOException exio) {
            setDefaultProperties();
            LOGGER.warn("Loading default properties", exio);
        }
        loadOptionalExternalConfig();
        loadOptionalDotEnv();
        applyEnvironmentOverrides();
        normalizePostgresJdbcUrl();
    }

    private void normalizePostgresJdbcUrl() {
        final String key = JDBC_URL_KEY;
        final String url = this.applicationProperties.getProperty(key);
        if (url == null || !url.contains("postgresql:") || url.contains("sslmode=")) {
            return;
        }
        final String params = "sslmode=disable&connectTimeout=30&socketTimeout=60";
        final String normalized = url.contains("?") ? url + "&" + params : url + "?" + params;
        this.applicationProperties.setProperty(key, normalized);
        LOGGER.debug("Normalized JDBC URL for remote PostgreSQL (ssl/timeouts).");
    }

    private void loadOptionalExternalConfig() {
        final String configPath = resolveExternalConfigPath();
        if (configPath == null || configPath.isBlank()) {
            return;
        }
        final Path path = Path.of(configPath);
        if (!Files.isRegularFile(path)) {
            LOGGER.warn("External config file not found: {}", configPath);
            return;
        }
        try (InputStream in = new FileInputStream(path.toFile())) {
            this.applicationProperties.load(in);
            LOGGER.info("Loaded external config from {}", configPath);
        } catch (final IOException ex) {
            LOGGER.warn("Failed to load external config from {}", configPath, ex);
        }
    }

    private String resolveExternalConfigPath() {
        final String fromProperty = System.getProperty(CONFIG_FILE_PROPERTY);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        final String fromEnv = System.getenv(CONFIG_FILE_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return null;
    }

    private void loadOptionalDotEnv() {
        final Path envPath = resolveDotEnvPath();
        if (envPath == null || !Files.isRegularFile(envPath)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                final String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                applyEnvKeyToProperties(key, value);
            }
            LOGGER.info("Loaded environment file from {}", envPath);
        } catch (final IOException ex) {
            LOGGER.warn("Failed to load environment file from {}", envPath, ex);
        }
    }

    private Path resolveDotEnvPath() {
        final String explicit = System.getenv(ENV_FILE_ENV);
        if (explicit != null && !explicit.isBlank()) {
            return Path.of(explicit);
        }
        final String configPath = resolveExternalConfigPath();
        if (configPath != null && !configPath.isBlank()) {
            final Path sibling = Path.of(configPath).getParent().resolve(".env");
            if (Files.isRegularFile(sibling)) {
                return sibling;
            }
        }
        final Path cwd = Path.of(System.getProperty("user.dir", ".")).resolve(".env");
        if (Files.isRegularFile(cwd)) {
            return cwd;
        }
        return null;
    }

    private void applyEnvironmentOverrides() {
        for (final EnvMapping mapping : ENV_MAPPINGS) {
            final String value = System.getenv(mapping.envName());
            if (value != null && !value.isBlank()) {
                this.applicationProperties.setProperty(mapping.propertyKey(), value);
            }
        }
    }

    private void applyEnvKeyToProperties(final String key, final String value) {
        for (final EnvMapping mapping : ENV_MAPPINGS) {
            if (mapping.envName().equals(key)) {
                this.applicationProperties.setProperty(mapping.propertyKey(), value);
                return;
            }
        }
    }

    private record EnvMapping(String envName, String propertyKey) {
    }

    private void setDefaultProperties() {
        this.applicationProperties.setProperty(REPOSITORY_FACTORY_KEY,
                "eapli.aisafe.persistence.impl.jpa.JpaRepositoryFactory");
        this.applicationProperties.setProperty(UI_MENU_LAYOUT_KEY, "horizontal");
        this.applicationProperties.setProperty(PERSISTENCE_UNIT_KEY, "eapli.aisafe");
        this.applicationProperties.setProperty("jakarta.persistence.schema-generation.database.action", "create");
    }

    public Boolean isMenuLayoutHorizontal() {
        return "horizontal".equalsIgnoreCase(this.applicationProperties.getProperty(UI_MENU_LAYOUT_KEY));
    }

    public String getPersistenceUnitName() {
        return this.applicationProperties.getProperty(PERSISTENCE_UNIT_KEY);
    }

    public String getRepositoryFactory() {
        return this.applicationProperties.getProperty(REPOSITORY_FACTORY_KEY);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map getExtendedPersistenceProperties() {
        final Map ret = new HashMap();
        for (final String key : this.applicationProperties.stringPropertyNames()) {
            if (isExtendedPersistenceKey(key)) {
                ret.put(key, this.applicationProperties.getProperty(key));
            }
        }
        for (final EnvMapping mapping : ENV_MAPPINGS) {
            final String value = this.applicationProperties.getProperty(mapping.propertyKey());
            if (value != null && !value.isBlank()) {
                ret.put(mapping.propertyKey(), value);
            }
        }
        return ret;
    }

    private static boolean isExtendedPersistenceKey(final String key) {
        for (final String prefix : EXTENDED_PERSISTENCE_PREFIXES) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public String getProperty(final String prop) {
        return this.applicationProperties.getProperty(prop);
    }
}
