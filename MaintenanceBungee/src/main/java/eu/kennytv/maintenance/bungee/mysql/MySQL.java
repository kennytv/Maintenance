package eu.kennytv.maintenance.bungee.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public final class MySQL {
    private final HikariDataSource hikariDataSource;

    public MySQL(final String hostname, final int port, final String username, final String password, final String database) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.addDataSourceProperty("serverName", hostname);
        hikariConfig.addDataSourceProperty("user", username);
        hikariConfig.addDataSourceProperty("password", password);
        hikariConfig.addDataSourceProperty("url", "jdbc:mysql://" + hostname + ":" + port + "/" + database);
        hikariConfig.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
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
            System.out.println("Error while executing update method: " + query);
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
            System.out.println("Error while executing query method: " + query);
            e.printStackTrace();
        }
    }

    public void close() {
        if (hikariDataSource != null && !hikariDataSource.isClosed())
            hikariDataSource.close();
    }
}
