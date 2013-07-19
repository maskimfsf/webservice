package com.eastcor.fishbowlconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

@Path("/login")
public class LoginService {

	public static final String password = "wux5kAy5"; // randomly generated
	public static final long timeout = 2700000L; // in milliseconds. set to 45 minutes.

	@POST
	@Consumes("application/json")
	@Produces("application/xml")
	public String login(@FormParam("user") String user,
			@FormParam("pass") String pass) throws ClassNotFoundException {
		System.out.println(user + " is trying to login.");
		String token = "", errorMsg = "";
		Connection conn;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver");

			conn = DriverManager.getConnection(
					"jdbc:firebirdsql:localhost/3050:c:/eastcor.fdb", "gone",
					"fishing");
			conn.setAutoCommit(false);
			PreparedStatement ps = conn
					.prepareStatement("select 'true' from sysuser where username = ? and userpwd = ?");
			ps.setString(1, user);
			ps.setString(2, pass.trim());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				PreparedStatement insertKey = conn.prepareStatement("insert into androidtokens (token) values(?)");
				String key = UUID.randomUUID().toString().toUpperCase() + "|"
						+ "com.eastcor.purchaseorder" + "|" + user + "|"
						+ System.currentTimeMillis();
				StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
				jasypt.setPassword(password);
				token = jasypt.encrypt(key);
				insertKey.setString(1, token);
				insertKey.execute();
				conn.commit();
				conn.close();
				System.out.println("Token created. Login successful.");
			}
		} catch (SQLException e) {
			errorMsg = e.getMessage();
		} 
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?><token>" + token
				+ "</token>\n<error>"+errorMsg+"</error>";

	}

}
