/**
 * 
 */
package org.dbwiki.driver.rdbms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbwiki.data.annotation.Annotation;
import org.dbwiki.data.annotation.AnnotationList;
import org.dbwiki.data.provenance.Provenance;
import org.dbwiki.data.provenance.ProvenanceFactory;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.time.Version;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;


/**
 * @author Snehal
 *
 */

/*
 * This is the new class added for the SQL queries written for all requests of visualisation of provenance and annotation
 * And also for provenance comparison request 
 */
public class ProvenanceGraphSQLQueries implements DatabaseConstants {
	
	/*
	 * Contains all SQL queries for provenance chart request
	 */
	public static String ProvenanceChartsSqlQueries(Connection con, RDBMSDatabase database, 
			String startDate, String endDate) throws SQLException {
		Statement statement = con.createStatement();
		String sqlQuery = "";
		String sqlResult = "";
		boolean startFlag = true;
		String username = "";
		String userList = "";
		int count = 0;
		String countList = "";
		
		if((startDate.isEmpty()|| startDate==null) && (endDate.isEmpty()|| endDate==null))
			sqlQuery = "SELECT full_name as username, count(*) as cnt FROM " + database.name() + RelationVersion + " v, _user u where v."+ RelVersionColUser +"=u.id group by full_name";
		else 
			sqlQuery = "SELECT full_name as username, count(*) as cnt FROM " + database.name() + RelationVersion + " v, _user u where v."+ RelVersionColUser +"=u.id and v.name::timestamp > '" +  startDate + "' and v.name::timestamp < '" + endDate + "' group by full_name";
		
		ResultSet rs = statement.executeQuery(sqlQuery);
		while (rs.next()) {
			username= rs.getString("username");
			count = rs.getInt("cnt");
			if(startFlag)
			{
				userList = username;
				countList = "" + count;
				startFlag = false;
			}
			else
			{
				userList = userList + "," + username;
				countList = countList + "," + count;
			}	
		}
		sqlResult = userList + ";" + countList;
		rs.close();
		statement.close();	
		return sqlResult;
	}
	
	/*
	 * Contains all SQL queries for provenance list request
	 */
	public static VersionIndex ProvenanceListSqlQueries(Connection con, RDBMSDatabase database, UserListing users, 
			String username, String startDate, String endDate) throws SQLException, WikiException {
		Statement statement = con.createStatement();
		VersionIndex versionList = new VersionIndex();
		String sqlQuery = "";
		String sqlResult = "";
		boolean startFlag = true;
		String userList = "";
		int count = 0;
		String countList = "";
		
		if(username.compareToIgnoreCase("all")==0 || username.isEmpty() || username == null)
			sqlQuery = "SELECT v."+ RelVersionColNumber +", " + RelVersionColName + ", " + RelVersionColNode + ", " + RelVersionColProvenance + ", " + RelVersionColSource + ", " + RelVersionColTime + ", " + RelVersionColUser +", u.full_name as user FROM " + database.name() + RelationVersion + " v, _user u where v."+ RelVersionColUser +"=u.id ORDER BY v." + RelVersionColNumber;
		else if((startDate.isEmpty()|| startDate==null) && (endDate.isEmpty()|| endDate==null))
			sqlQuery = "SELECT v."+ RelVersionColNumber +", " + RelVersionColName + ", " + RelVersionColNode + ", " + RelVersionColProvenance + ", " + RelVersionColSource + ", " + RelVersionColTime + ", " + RelVersionColUser +", u.full_name as user FROM " + database.name() + RelationVersion + " v, _user u where v."+ RelVersionColUser +"=u.id and u.full_name = '" + username + "' ORDER BY v." + RelVersionColNumber;
		else 
			sqlQuery = "SELECT v."+ RelVersionColNumber +", " + RelVersionColName + ", " + RelVersionColNode + ", " + RelVersionColProvenance + ", " + RelVersionColSource + ", " + RelVersionColTime + ", " + RelVersionColUser +", u.full_name as user FROM " + database.name() + RelationVersion + " v, _user u where v."+ RelVersionColUser +"=u.id and u.full_name = '" + username + "' and v." + RelVersionColName + "::timestamp > '" + startDate + "' and v." + RelVersionColName + "::timestamp < '" + endDate + "' ORDER BY v." + RelVersionColNumber;
		
		ResultSet rs = statement.executeQuery(sqlQuery);
		while (rs.next()) {
			int versionNumber = rs.getInt(RelVersionColNumber);
			String versionName = rs.getString(RelVersionColName);
			User user = users.get(rs.getInt(RelVersionColUser));
			int nodeId = rs.getInt(RelVersionColNode);
			NodeIdentifier nid = null;
			if(nodeId != -1)
			nid = new NodeIdentifier(nodeId);
			Provenance provenance =
				ProvenanceFactory.getProvenance((byte)rs.getInt(RelVersionColProvenance),
					user, nid, rs.getString(RelVersionColSource));
			long createTime = rs.getLong(RelVersionColTime);
			
			versionList.add(new Version(versionNumber, versionName, createTime, provenance, versionList)); 
		}
		rs.close();
		statement.close();	
		return versionList;
	}
	
	/*
	 * Contains all SQL queries for annotation chart request
	 */
	public static String AnnotationChartsSqlQueries(Connection con,
			RDBMSDatabase database, String startDate, String endDate) throws SQLException {
		
		Statement statement = con.createStatement();
		String sqlQuery = "";
		String sqlQueryForUnknownUser = "";
		String sqlResult = "";
		boolean startFlag = true;
		String username = "";
		String userList = "";
		int count = 0;
		String countList = "";
		
		if((startDate.isEmpty()|| startDate==null) && (endDate.isEmpty()|| endDate==null))
		{	
			sqlQuery = "SELECT full_name as username, count(*) as cnt FROM " + database.name() + RelationAnnotation + " a, _user u where a."+ RelAnnotationColUser +"=u.id group by full_name";
			sqlQueryForUnknownUser = "SELECT count(*) as cnt FROM " + database.name() + RelationAnnotation + " where "+ RelAnnotationColUser +"=-1";
		}
		else 
		{
			sqlQuery = "SELECT full_name as username, count(*) as cnt FROM " + database.name() + RelationAnnotation + " a,_user u where a."+ RelAnnotationColUser +"=u.id and date::timestamp > '" +  startDate + "' and date::timestamp < '" + endDate + "' group by full_name";
			sqlQueryForUnknownUser = "SELECT count(*) as cnt FROM " + database.name() + RelationAnnotation + " where "+ RelAnnotationColUser +"=-1 and date::timestamp > '" +  startDate + "' and date::timestamp < '" + endDate + "'";
		}
		
		ResultSet rs = statement.executeQuery(sqlQuery);
		while (rs.next()) {
			username= rs.getString("username");
			count = rs.getInt("cnt");
			if(startFlag)
			{
				userList = username;
				countList = "" + count;
				startFlag = false;
			}
			else
			{
				userList = userList + "," + username;
				countList = countList + "," + count;
			}	
		}
		
		//for unknown user
		ResultSet rs1 = statement.executeQuery(sqlQueryForUnknownUser);
		while (rs1.next()) {
			username= "Unknown";
			count = rs1.getInt("cnt");
			if(count !=0 )
			{
				userList = userList + "," + username;
				countList = countList + "," + count;
			}	
		}
		
		sqlResult = userList + ";" + countList;

		rs.close();
		rs1.close();
		statement.close();	
		return sqlResult;
	}
	
	/*
	 * Contains all SQL queries for annotation list request
	 */
	public static AnnotationList AnnotationTableSQLQuery(Connection con, RDBMSDatabase database,
			String username, String startDate, String endDate) throws SQLException {
		// TODO Auto-generated method stub
	
			Statement statement = con.createStatement();
			AnnotationList annotations = new AnnotationList();
			String sqlQuery = "";
			String sqlQueryForUnknown = "";
				
			if(username.compareToIgnoreCase("all")==0 ||username.isEmpty() || username == null)
			{
				if((startDate.isEmpty()|| startDate==null) && (endDate.isEmpty()|| endDate==null))
				{	
					sqlQuery = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColUser + ", " + RelAnnotationColText + ", u.full_name as user FROM " + database.name() + RelationAnnotation + " a,_user u where a."+ RelAnnotationColUser +"=u.id";
					sqlQueryForUnknown = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColUser + ", " + RelAnnotationColText + ", 'Unknown' as user FROM " + database.name() + RelationAnnotation + " a where a."+ RelAnnotationColUser +"='-1'";
				}
				else
				{
					sqlQuery = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColUser + ", " + RelAnnotationColText + ", u.full_name as user FROM " + database.name() + RelationAnnotation + " a,_user u where a."+ RelAnnotationColUser +"=u.id and " + RelAnnotationColDate + "::timestamp > '" +  startDate + "' and " + RelAnnotationColDate + "::timestamp < '" + endDate + "'";
					sqlQueryForUnknown = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColUser + ", " + RelAnnotationColText + ", 'Unknown' as user FROM " + database.name() + RelationAnnotation + " a where a."+ RelAnnotationColUser +"='-1' and " + RelAnnotationColDate + "::timestamp > '" +  startDate + "' and " + RelAnnotationColDate + "::timestamp < '" + endDate + "'";
				}
			}
			else if((startDate.isEmpty()|| startDate==null) && (endDate.isEmpty()|| endDate==null))
			{
				if (username.compareTo("Unknown") != 0)
					sqlQuery = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColUser + ", " + RelAnnotationColText + ", u.full_name as user FROM " + database.name() + RelationAnnotation + " a,_user u where a."+ RelAnnotationColUser +"=u.id and u.full_name = '" + username + "'";
				else
					sqlQuery = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColText + ", " + RelAnnotationColUser + " FROM " + database.name() + RelationAnnotation + " a where a."+ RelAnnotationColUser +"='-1'";
			}
			else 
			{
				if (username.compareTo("Unknown") != 0)
					sqlQuery = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColUser + ", " + RelAnnotationColText + ", u.full_name as user FROM " + database.name() + RelationAnnotation + " a,_user u where a."+ RelAnnotationColUser +"=u.id and u.full_name = '" + username + "' and " + RelAnnotationColDate + "::timestamp > '" +  startDate + "' and " + RelAnnotationColDate + "::timestamp < '" + endDate + "'";
				else
					sqlQuery = "SELECT a." + RelAnnotationColID + ", " + RelAnnotationColNode + ", " + RelAnnotationColParent + ", " + RelAnnotationColDate + ", " + RelAnnotationColText + ", " + RelAnnotationColUser + " FROM " + database.name() + RelationAnnotation + " a where a."+ RelAnnotationColUser +"='-1' and " + RelAnnotationColDate + "::timestamp > '" +  startDate + "' and " + RelAnnotationColDate + "::timestamp < '" + endDate + "'";
			}
			
			ResultSet rs = statement.executeQuery(sqlQuery);
			while (rs.next()) {
				int id = rs.getInt(RelAnnotationColID);
				int node = rs.getInt(RelAnnotationColNode);
				int parent = rs.getInt(RelAnnotationColParent);
				String date = rs.getString(RelAnnotationColDate);
				String text = rs.getString(RelAnnotationColText);
				User user =database.users().get(rs.getInt(RelAnnotationColUser));
				annotations.add(new Annotation(id, text, date, user));
			}
			
			if(username.compareToIgnoreCase("all")==0 ||username.isEmpty() || username == null)
			{
				ResultSet rs1 = statement.executeQuery(sqlQueryForUnknown);
				while (rs1.next()) {
					int id = rs1.getInt(RelAnnotationColID);
					int node = rs1.getInt(RelAnnotationColNode);
					int parent = rs1.getInt(RelAnnotationColParent);
					String date = rs1.getString(RelAnnotationColDate);
					String text = rs1.getString(RelAnnotationColText);
					User user =database.users().get(rs1.getInt(RelAnnotationColUser));
					annotations.add(new Annotation(id, text, date, user));
				}
				rs1.close();
			}
			rs.close();
			statement.close();	
			return annotations;
	}
	
	
	
	/*
	 * Contains SQL query for provenance comparison request
	 */
	public static String compareVersion(Connection con, RDBMSDatabase database,
			String version) throws SQLException {
		// TODO Auto-generated method stub
	
			Statement statement = con.createStatement();
			String sqlQuery = "";
			String result = "";
				
			sqlQuery = "select value from ciawfb_data where id='"+ version +"'";
			ResultSet rs = statement.executeQuery(sqlQuery);
			while (rs.next()) {
				result = rs.getString("value");
			}
			return result;
	}
}
