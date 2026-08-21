package com.tilldock.auth.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class PasswordResetTool {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("DATABASE_URL");
        String email = args.length > 0 ? args[0] : "emulator20260818@example.com";
        String newPassword = args.length > 1 ? args[1] : "Test1234";
        if (url == null) {
            System.err.println("DATABASE_URL not set");
            System.exit(1);
        }
        Class.forName("org.postgresql.Driver");
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = enc.encode(newPassword);
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement ps = c.prepareStatement("UPDATE merchants SET password_hash = ? WHERE email = ?")) {
            ps.setString(1, hash);
            ps.setString(2, email);
            int n = ps.executeUpdate();
            System.out.println("Updated " + n + " row(s) for email=" + email + " password=" + newPassword);
        }
    }
}
