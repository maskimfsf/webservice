package com.eastcor.fishbowlconnect;

import java.sql.*;

public class FirebirdDatabaseHandler {
	public Connection conn;


	public FirebirdDatabaseHandler() {
		try {
			conn = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050c://eastcor.fdb", "gone",
					"fishing");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
