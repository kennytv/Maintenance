package eu.kennytv.maintenance.core.proxy.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MySQL {
    private final Logger logger;
    private final HikariDataSource hikariDataSource;

    // Excuse me and everything to do with the MySQL stuff, I have little to no idea of how to use databases
    public MySQL(final Logger logger, final String hostname, final int port, final String username, final String password, final String database, final boolean useSSL) {
        this.logger = logger;

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.addDataSourceProperty("serverName", hostname);
        hikariConfig.addDataSourceProperty("user", username);
        hikariConfig.addDataSourceProperty("password", password);

        String urlProperty = "jdbc:mysql://" + hostname + ":" + port + "/" + database;
        if (useSSL) {
            urlProperty += "?useSSL=false";
        }
        hikariConfig.addDataSourceProperty("url", urlProperty);

        hikariConfig.setJdbcUrl(urlProperty);
        hikariConfig.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void executeUpdate(final String query, final Consumer<Integer> callback, final Object... objects) {
        try (final Connection connection = hikariDataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                int current = 1;
                for (final Object object : objects) {
                    preparedStatement.setObject(current, object);
                    current++;
                }

                callback.accept(preparedStatement.executeUpdate());
            }
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Error while executing update method: " + query);
            e.printStackTrace();
        }
    }

    public void executeQuery(final String query, final Consumer<ResultSet> callback, final Object... objects) {
        try (final Connection connection = hikariDataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                int current = 1;
                for (final Object object : objects) {
                    preparedStatement.setObject(current, object);
                    current++;
                }

                final ResultSet resultSet = preparedStatement.executeQuery();
                callback.accept(resultSet);
                resultSet.close();
            }
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "Error while executing query method: " + query);
            e.printStackTrace();
        }
    }

    public void executeUpdate(final String query, final Object... objects) {
        executeUpdate(query, res -> {
        }, objects);
    }

    public void close() {
        hikariDataSource.close();
    }
}
