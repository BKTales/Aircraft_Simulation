import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** One-off: CREATE DATABASE aisafe on vs233 (connect to postgres DB first). */
public class CreateAisafeDb {
    public static void main(String[] args) throws Exception {
        final String jdbcUrl = System.getenv("AISAFE_JDBC_URL");
        final String url = adminUrl(jdbcUrl != null ? jdbcUrl : "");
        final String user = System.getenv().getOrDefault("AISAFE_JDBC_USER", "postgres");
        final String pass = System.getenv("AISAFE_DB_PASSWORD");
        if (pass == null || pass.isBlank()) {
            System.err.println("Set AISAFE_DB_PASSWORD (or use .env)");
            System.exit(1);
        }
        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = 'aisafe'")) {
                if (rs.next()) {
                    System.out.println("Database aisafe already exists");
                    return;
                }
            }
            s.executeUpdate("CREATE DATABASE aisafe");
            System.out.println("CREATE DATABASE aisafe OK");
        }
    }

    private static String adminUrl(final String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return "jdbc:postgresql://vsgate-s1.dei.isep.ipp.pt:10233/postgres";
        }
        final int slash = jdbcUrl.lastIndexOf('/');
        if (slash < 0) {
            return jdbcUrl + "/postgres";
        }
        return jdbcUrl.substring(0, slash + 1) + "postgres";
    }
}
