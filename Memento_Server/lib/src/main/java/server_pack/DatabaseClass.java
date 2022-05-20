package server_pack;

import java.sql.Timestamp;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseClass {
	Statement statement;
	Connection databaseConnection;
	//private static Connection connection;
	public DatabaseClass() {
		// TODO Auto-generated constructor stub
		try {
			databaseConnection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres", "postgres", "Muffin88");
			if(databaseConnection.isValid(0)) {
			    System.out.println("Connected to PostgreSQL database!");
			    statement = databaseConnection.createStatement();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public boolean setData(String phonenumber, int groupId, byte[] media, String category) {
		// TODO Auto-generated method stub
		int dataSize = media.length;
		Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
		try {
			statement.execute("CREATE TABLE IF NOT EXISTS public.shared_data"
					+ " Time timestamp NOT NULL,"
					+ " GroupId varchar(10) NOT NULL,"
					+ " Data varbinary(8000),"
					+ " Category varchar(20),"
					+ " DataSize int,"
					+ " PRIMARY KEY (Time, GroupId)");
			String sqlString = "INSERT INTO public.shared_data (Time, GroupId, Data) VALUES(?, ?, ?, ?, ?)";
			PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
			pst.setTimestamp(1, timestamp);
			pst.setInt(2, groupId);
			pst.setBytes(3, media);
			pst.setString(4, category);
			pst.setLong(5, dataSize);
			if(pst.executeLargeUpdate() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public int setDeceased(String name,  String bDay, String dDay) {
		// TODO Auto-generated method stub
		try {
			statement.execute("CREATE TABLE IF NOT EXISTS public.deceased"
					+ " (GroupId INT NOT NULL GENERATED ALWAYS AS IDENTITY,"
					+ " Name varchar(30) NOT NULL,"
					+ " BirthDate varchar(12),"
					+ " DeceasedDate varchar(12),"
					+ " PRIMARY KEY(GroupId))");
			
			String sqlString = "INSERT INTO public.Deceased (Name, BirthDate, DeceasedDate) VALUES (?, ?, ?) RETURNING GroupId";
			PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
			pst.setString(1, name);
			pst.setString(2, bDay);
			pst.setString(3, dDay);			
			ResultSet keys = pst.executeQuery();	
			if(keys.next()) 
				return keys.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 0;
		
	}
	
	public boolean register(String phonenumber) throws SQLException {
		statement = databaseConnection.createStatement();
		String sqlString = "INSERT INTO  public.users (PhoneNumber) VALUES(?)";
		try {
			statement.execute("CREATE TABLE IF NOT EXISTS public.users"
					+ "(PhoneNumber varchar(10) not NULL, "
					+ " PRIMARY KEY (PhoneNumber))");
			
			PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
			pst.setString(1, phonenumber);
			pst.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean isRegesterdUser(String phoneNumber) throws SQLException {
		String sqlString = "SELECT * FROM public.users WHERE PhoneNumber = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		ResultSet resultSet = pst.executeQuery();
		return resultSet.next();
	}
	
	public void storePendingInvites(String phoneNumber, int groupId, String groupName, boolean admin) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS public.invites"
				+ "(PhoneNumber varchar(10) not NULL, "
				+ " GroupId int,"
				+ " GroupName varchar(40)"
				+ " Admin BOOLEAN,"
				+ " PRIMARY KEY (PhoneNumber, GroupId))");
		String sqlString = "INSERT INTO  public.invites (PhoneNumber, GroupId, GroupName, Admin) VALUES(?, ?, ?, ?)";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		pst.setInt(2, groupId);
		pst.setString(3, groupName);
		pst.setBoolean(4, admin);	
		pst.executeQuery();
		
	}
	
	public ResultSet getPendingInvites(String phoneNumber) throws SQLException {
		String sqlString = "SELECT * FROM public.invites WHERE PhoneNumber = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		return pst.executeQuery();
		
	}
	
	public void removePendingInvites(String phoneNumber, int groupId) throws SQLException {
		String sqlString = "DELETE * FROM public.invites WHERE PhoneNumber = ? AND GroupId = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		pst.setInt(2, groupId);
		pst.executeQuery();
	}
	
	public ResultSet getUpdates(int groupId, java.sql.Timestamp lastUpdate, Timestamp now) throws SQLException {
		String sqlString = "SELECT * FROM public.shared_data WHERE GroupId = ? AND WHERE Time BETWEEN ? AND ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		pst.setTimestamp(2, lastUpdate);
		pst.setTimestamp(3, now);
		ResultSet updatSet = pst.executeQuery();
		return updatSet;
		
	}
	
	public void removeOldData(int groupId) throws SQLException {
		String sqlString = "DELETE * FROM public.shared_data WHERE GroupId = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		pst.executeQuery();
	}

}