/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    // Excuse me and everything to do with the MySQL stuff, I have little to no idea of how to use databases
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

    public void executeUpdate(final String query, final Object... objects) {
        executeUpdate(query, res -> {
        }, objects);
    }
}
