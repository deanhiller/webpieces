package webpiecesxxxxxpackage.db;

public class DbCredentials {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DbCredentials(final String jdbcUrl, final String username, final String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;

    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
