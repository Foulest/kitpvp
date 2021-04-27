package net.foulest.kitpvp.util;

import net.foulest.kitpvp.KitPvP;

import java.sql.*;

/**
 * @author Foulest
 * @project KitPvP
 */
public class MySQL {

    private static final MySQL INSTANCE = new MySQL();
    private static final KitPvP KITPVP = KitPvP.getInstance();

    public static MySQL getInstance() {
        return INSTANCE;
    }

    public static boolean exists(String scope, String table, String column, String logicGate, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }

        try (Connection connection = KITPVP.getHikari().getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT " + scope + " FROM "
                     + table + " WHERE " + column + logicGate + data)) {
            ResultSet result = select.executeQuery();

            if (result.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void update(String command) {
        try (Connection connection = KITPVP.getHikari().getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(command);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet query(String scope, String table, String column, String logicGate, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }

        ResultSet result = null;

        try (Connection connection = KITPVP.getHikari().getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT " + scope + " FROM "
                     + table + " WHERE " + column + logicGate + data)) {
            result = select.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Object get(String selected, String scope, String table, String column, String logicGate, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }

        ResultSet result;

        try (Connection connection = KITPVP.getHikari().getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT " + scope + " FROM "
                     + table + " WHERE " + column + logicGate + data)) {
            result = select.executeQuery();

            if (result != null && result.next()) {
                return result.getObject(selected);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
