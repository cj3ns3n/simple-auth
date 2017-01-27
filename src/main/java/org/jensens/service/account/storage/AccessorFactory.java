package org.jensens.service.account.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AccessorFactory {
    public static Accessor getAccountAccessor() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File dbFile = new File(tempDir, "Accounts");
        String connURL = String.format("jdbc:derby:%s;create=true", dbFile.getPath());
        //final String connURL = "jdbc:derby:memory:accounts;create=true";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connURL);
        } catch (SQLException e) {
            e.printStackTrace();

            // Filed to make file based DB, use in memory DB.  Just so stuff works.
            connURL = "jdbc:derby:memory:accounts;create=true";
            try {
                conn = DriverManager.getConnection(connURL);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        try {
            return new DerbyAccessor(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
