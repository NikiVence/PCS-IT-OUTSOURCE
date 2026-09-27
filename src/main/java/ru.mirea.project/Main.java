package ru.mirea.project;

import ru.mirea.project.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Проверка подключения к PostgreSQL ===");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            System.out.println("✅ Подключение успешно!");
            System.out.println("   URL:      " + DatabaseManager.getInstance().getUrl());
            System.out.println("   User:     " + DatabaseManager.getInstance().getUsername());
            System.out.println("   Catalog:  " + conn.getCatalog());
            System.out.println("   DB:       " + conn.getMetaData().getDatabaseProductName()
                    + " " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println();

            System.out.println("=== Проверка таблиц ===");
            checkCount(conn, "users");
            checkCount(conn, "requests");

        } catch (SQLException e) {
            System.err.println("❌ Ошибка SQL: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState());
        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * SELECT COUNT(*) FROM {table} — демонстрация PreparedStatement + ResultSet.
     * Имя таблицы захардкожено в коде (не user input), поэтому конкатенация безопасна.
     */
    private static void checkCount(Connection conn, String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("   ✅ " + table + ": " + rs.getInt(1) + " записей");
            }
        } catch (SQLException e) {
            System.err.println("   ❌ " + table + ": " + e.getMessage());
        }
    }
}