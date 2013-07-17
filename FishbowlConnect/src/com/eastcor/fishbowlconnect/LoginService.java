package com.eastcor.fishbowlconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

@Path("/login")
public class LoginService {

	public static final String password = "wux5kAy5";

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public String login(@FormParam("user") String user,
			@FormParam("pass") String pass) throws ClassNotFoundException {
		System.out.println("invoked: " + user + " " + pass);
		boolean success = false;
		String token = "";
		Connection conn;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver");

			conn = DriverManager.getConnection(
					"jdbc:firebirdsql:localhost/3050:c:/eastcor.fdb", "gone",
					"fishing");
			PreparedStatement ps = conn
					.prepareStatement("select 'true' from sysuser where username = ? and userpwd = ?");
			ps.setString(1, user);
			ps.setString(2, pass);
			ResultSet rs = ps.executeQuery();
			success = rs.next();
			System.out.println(user + " " + pass + " " + success);
			if (success) {
				PreparedStatement insertKey = conn.prepareStatement("insert into androidtokens (token) values(?)");
				DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
				Date d = new Date();
				String key = UUID.randomUUID().toString().toUpperCase() + "|"
						+ "com.eastcor.purchaseorder" + "|" + user + "|"
						+ df.format(d);
				System.out.println(key);
				StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
				jasypt.setPassword(password);
				token = jasypt.encrypt(key);
				System.out.println(token);
				insertKey.setString(1, token);
				insertKey.execute();
				System.out.println(jasypt.decrypt(token));
				conn.commit();
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?><token>" + token
				+ "</token>";

	}

}
