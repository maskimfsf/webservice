package com.eastcor.fishbowlconnect;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Path("/query")
public class PurchaseOrderService {

	@Path("/update")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public String updatePoList(@FormParam("token") String token,
			@FormParam("xml") String xml) {
		System.out.println("Received update list");
		Connection conn;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver");
			conn = DriverManager.getConnection(
					"jdbc:firebirdsql:localhost/3050:c:/eastcor.fdb", "gone",
					"fishing");
			conn.setAutoCommit(true);
			String updateQuery = "update customset set info = ? where recordid = (select id from po where num = ?)";
			String insertToCustomVarcharLong = "insert into customvarcharlong (id, customfieldid, info, recordid) values (gen_id(GENCUSTOMVARCHARLONGID, 1), 23, ?, (select id from po where num = ?))";
			String insertToCustomInteger = "insert into custominteger (id, customfieldid, info, recordid) values (gen_id(GENCUSTOMINTEGERID, 1), 22, 1, (select id from po where num = ?))";
			String insertToCustomTimestamp = "insert into customtimestamp (id, customfieldid, info, recordid) values (gen_id(GENCUSTOMTIMESTAMPID, 1), 28, current_timestamp, (select id from po where num = ?))";
			
			
			PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
			PreparedStatement insertToCustomVarcharLongStatement = conn.prepareStatement(insertToCustomVarcharLong);
			PreparedStatement insertToCustomIntegerStatement = conn.prepareStatement(insertToCustomInteger);
			PreparedStatement insertToCustomTimestampStatement = conn.prepareStatement(insertToCustomTimestamp);
			
			if (!checkToken(conn, token)) {
				throw new TokenException("Error connecting to database.");
			} else if (xml != null && !xml.equals("")) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				dbFactory.setIgnoringElementContentWhitespace(true);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse((new InputSource(
						new StringReader(xml))));
				doc.getDocumentElement().normalize();
				NodeList nodes = doc.getElementsByTagName("polist");
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						NodeList pos = element.getElementsByTagName("po");
						for (int j = 0; j < pos.getLength(); j++) {
							NodeList subNodes = pos.item(j).getChildNodes();
							int number = Integer.parseInt(subNodes.item(0).getTextContent());
							String status = subNodes.item(1).getTextContent();
							System.out.println("\n" + number + ": " + status);
							updateStatement.setString(1, status);
							updateStatement.setInt(2, number);
							if(status.equals("Rejected")) {
								String reason = URLDecoder.decode(subNodes.item(2).getTextContent(), "UTF-8");
								System.out.println("Reason: " + reason);
								
								insertToCustomVarcharLongStatement.setString(1, reason);
								insertToCustomVarcharLongStatement.setInt(2, number);
								insertToCustomVarcharLongStatement.execute();
								
								insertToCustomIntegerStatement.setInt(1, number);
								insertToCustomIntegerStatement.execute();
								
								insertToCustomTimestampStatement.setInt(1, number);
								insertToCustomTimestampStatement.execute();
							}
							updateStatement.execute();
						}
					}
				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "true";
	}

	@Path("/list")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public String getPoList(@FormParam("token") String token)
			throws ClassNotFoundException, TokenException {
		System.out.println("getPoList invoked.");
		Connection conn;
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<polist>";
		String getReportInfo = "select po.id as \"po.id\", num, "
				+ "remittoname, " + "remitaddress, " + "remitcity, "
				+ "remitzip, " + "buyer, " + "shiptoname, " + "shiptoaddress, "
				+ "shiptocity, " + "shiptozip, " + "dateissued, "
				+ "shipterms.name as \"shipterms.name\", "
				+ "carrier.name as \"carrier.name\", "
				+ "paymentterms.name as \"paymentterms.name\", "
				+ "fobpoint.name as \"fobpoint.name\" "

				+ "from po " + "join customset on po.id = customset.recordid "
				+ "join shipterms on shipterms.id = po.shiptermsid "
				+ "join carrier on carrier.id = po.carrierid "
				+ "join paymentterms on paymentterms.id = po.paymenttermsid "
				+ "join fobpoint on po.fobpointid = fobpoint.id "

				+ "where customset.info = \'Waiting for Approval\' "
				+ "order by num";
		String items = "select description, totalcost from poitem where poid = ?";
		ResultSet rs;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver");

			conn = DriverManager.getConnection(
					"jdbc:firebirdsql:localhost/3050:c:/eastcor.fdb", "gone",
					"fishing");
			//conn.setAutoCommit(false);

			if (!checkToken(conn, token)) {
				throw new TokenException("Error connecting to database.");
			}
			Statement statement = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.HOLD_CURSORS_OVER_COMMIT);
			rs = statement.executeQuery(getReportInfo);

			while (rs.next()) {
				// really really ugly, but need to check for null before sending
				// to XML.escape
				xml += "\n\n\t<purchaseorder>\n";
				int id = rs.getInt("po.id");
				xml += "\t\t<ponum>" + rs.getInt("num") + "</ponum>\n";
				// start vendor
				xml += "\t\t<vendorname>"
						+ XML.escape(checkNull(rs.getString("remittoname")))
						+ "</vendorname>\n";
				xml += "\t\t<vendoraddress>"
						+ XML.escape(checkNull(rs.getString("remitaddress")))
						+ "</vendoraddress>\n";
				xml += "\t\t<vendorcity>"
						+ XML.escape(checkNull(rs.getString("remitcity")))
						+ "</vendorcity>\n";
				xml += "\t\t<vendorzip>"
						+ XML.escape(checkNull(rs.getString("remitzip")))
						+ "</vendorzip>\n";
				// end vendor
				// begin ship to
				xml += "\t\t<shiptoname>"
						+ XML.escape(checkNull(rs.getString("shiptoname")))
						+ "</shiptoname>\n";
				xml += "\t\t<shiptoaddress>"
						+ XML.escape(checkNull(rs.getString("shiptoaddress")))
						+ "</shiptoaddress>\n";
				xml += "\t\t<shiptocity>"
						+ XML.escape(checkNull(rs.getString("shiptocity")))
						+ "</shiptocity>\n";
				xml += "\t\t<shiptozip>"
						+ XML.escape(checkNull(rs.getString("shiptozip")))
						+ "</shiptozip>\n";
				// end ship to
				xml += "\t\t<buyer>"
						+ XML.escape(checkNull(rs.getString("buyer")))
						+ "</buyer>\n";
				xml += "\t\t<dateissued>"
						+ XML.escape(checkNull(rs.getString("dateissued")))
						+ "</dateissued>\n";
				xml += "\t\t<shipterms>"
						+ XML.escape(checkNull(rs.getString("shipterms.name")))
						+ "</shipterms>\n";
				xml += "\t\t<carrier>"
						+ XML.escape(checkNull(rs.getString("carrier.name")))
						+ "</carrier>\n";
				xml += "\t\t<paymentterms>"
						+ XML.escape(checkNull(rs
								.getString("paymentterms.name")))
						+ "</paymentterms>\n";
				xml += "\t\t<fob>"
						+ XML.escape(checkNull(rs.getString("fobpoint.name")))
						+ "</fob>\n";
				xml += "\t\t<partlist>\n";

				// get part info here
				PreparedStatement getPartList = conn.prepareStatement(items);
				getPartList.setInt(1, id);
				ResultSet partList = getPartList.executeQuery();
				try {
					while (partList.next()) {
						xml += "\t\t\t<part>\n";
						xml += "\t\t\t\t<desc>"
								+ XML.escape(checkNull(partList
										.getString("description")))
								+ "</desc>\n";
						xml += "\t\t\t\t<cost>"
								+ XML.escape(checkNull(partList
										.getString("totalcost"))) + "</cost>\n";
						xml += "\t\t\t</part>\n";
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					partList.close();
				}

				xml += "\t\t</partlist>\n\t</purchaseorder>";
			}
			rs.close();
			conn.close();
			xml += "\n</polist>";

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return xml;

	}

	private boolean checkToken(Connection c, String token)
			throws TokenException {
		try {
			String statement = "select * from androidtokens where token = ?";
			PreparedStatement ps = c.prepareStatement(statement);
			ps.setString(1, token);
			StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
			jasypt.setPassword(LoginService.password);
			String tokenDecrypted = jasypt.decrypt(token);
			if (checkTimeout(Long.parseLong(tokenDecrypted
					.substring(tokenDecrypted.lastIndexOf("|") + 1)))) {
				throw new TokenException("Token expired");
			}
			return ps.execute();
		} catch (SQLException e) {
			return false;
		}
	}

	private boolean checkTimeout(long time) {
		return System.currentTimeMillis() - time > LoginService.timeout;

	}

	private String checkNull(String c) {
		return (c == null) ? "" : c;
	}

}
