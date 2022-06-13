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
	private Statement statement;
	private Connection databaseConnection;
	public DatabaseClass() {
		try {
			databaseConnection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres", "postgres", "Muffin88");
			if(databaseConnection.isValid(0)) {
			    
			    statement = databaseConnection.createStatement();
			    setTables();
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public void setTables() throws SQLException {
		
		statement.execute("CREATE TABLE IF NOT EXISTS public.users"
				+ "(phone_number varchar(10) not NULL, "
				+ " PRIMARY KEY (phone_number))");
		
		statement.execute("CREATE TABLE IF NOT EXISTS public.deceased"
				+ " (group_id INT NOT NULL GENERATED ALWAYS AS IDENTITY,"
				+ " name varchar(30) NOT NULL,"
				+ " birth_date varchar(12),"
				+ " deceased_date varchar(12),"
				+ " PRIMARY KEY(group_id))");
		
		statement.execute("CREATE TABLE IF NOT EXISTS public.shared_data"
				+ " (time timestamp NOT NULL,"
				+ " phone_number varchar(10) REFERENCES public.users (phone_number),"
				+ " group_id int REFERENCES public.deceased (group_id),"
				+ " data_type varchar(10),"
				+ " data bytea,"
				+ " category varchar(20),"
				+ " data_size int,"
				+ " PRIMARY KEY (time, group_id))");
		
		statement.execute("ALTER TABLE public.shared_data ALTER COLUMN data SET STORAGE EXTERNAL");
		
		statement.execute("CREATE TABLE IF NOT EXISTS public.group_members"
				+ "(phone_number varchar(10) REFERENCES public.users (phone_number),"
				+ " group_id int REFERENCES public.deceased (group_id),"
				+ " admin boolean,"
				+ " accepted boolean,"
				+ " PRIMARY KEY (phone_number, group_id))");
	}
	
	public void setData(String phoneNumber, int groupId, String type, byte[] media, String category) throws SQLException {
		int dataSize = media.length;
		Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
		String sqlString = "INSERT INTO public.shared_data (time, phone_number, group_id, data_type, data, category, data_size) VALUES(?, ?, ?, ?, ?, ?, ?) RETURNING group_id";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
			pst.setTimestamp(1, timestamp);
			pst.setString(2, phoneNumber);
			pst.setInt(3, groupId);
			pst.setString(4, type);
			pst.setBytes(5, media);
			pst.setString(6, category);
			pst.setLong(7, dataSize);
			ResultSet resultSet = pst.executeQuery();
			
			
		
	}
	
	public int setDeceased(String name,  String bDay, String dDay) throws SQLException {

			String sqlString = "INSERT INTO public.Deceased (name, birth_date, deceased_date) VALUES (?, ?, ?) RETURNING group_id";
			PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
			pst.setString(1, name);
			pst.setString(2, bDay);
			pst.setString(3, dDay);			
			ResultSet keys = pst.executeQuery();	
			if(keys.next()) 
				return keys.getInt(1);
			else
				return 0;
		
	}
	
	public void register(String phonenumber) throws SQLException {
		statement = databaseConnection.createStatement();
		String sqlString = "INSERT INTO  public.users (phone_number) VALUES(?)";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phonenumber);
		pst.executeUpdate();
	}
	
	public boolean isRegesterdUser(String phoneNumber) throws SQLException {
		String sqlString = "SELECT * FROM public.users WHERE phone_number = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		ResultSet resultSet = pst.executeQuery();
		return resultSet.next();
	}
	
	public int membersOfGroup(String inviter, String phoneNumber, int groupId, boolean admin, String groupName, String bDate, String dDate) throws SQLException {
		if(groupId == 0) {
			groupId = setDeceased(groupName, bDate, dDate);
		}
		
		String sqlString = "INSERT INTO  public.group_members (phone_number, group_id, admin, accepted) VALUES(?, ?, ?, false) RETURNING group_id";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		pst.setInt(2, groupId);
		pst.setBoolean(3, admin);	
		pst.executeQuery();
		
		sqlString = "INSERT INTO  public.group_members (phone_number, group_id, admin, accepted) VALUES(?, ?, true, true) RETURNING group_id";
		pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, inviter);
		pst.setInt(2, groupId);
		pst.executeQuery();
		return groupId;		
	}
	
	public ResultSet getPendingInvites(String phoneNumber) throws SQLException {
		String sqlString = "SELECT group_id, admin FROM public.group_members WHERE phone_number = ? AND accepted = false";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setString(1, phoneNumber);
		return pst.executeQuery();
		
	}
	
	
	
	public ResultSet getUpdates(String phoneNumber, int groupId, Timestamp lastUpdate) throws SQLException {
		String sqlString = "SELECT * FROM public.shared_data WHERE group_id = ? AND Time > ? AND phone_number != ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		pst.setTimestamp(2, lastUpdate);
		pst.setString(3, phoneNumber);
		ResultSet updatSet = pst.executeQuery();
		return updatSet;
		
	}
	
	public void removeOldData(int groupId) throws SQLException {
		String sqlString = "DELETE * FROM public.shared_data WHERE group_id = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		pst.executeQuery();
	}

	public void acceptInvite(String phoneNumber, int groupId) throws SQLException {
		String sqlString = "UPDATE public.group_members SET accepted = true WHERE group_id = ? AND phone_number = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		pst.setString(2, phoneNumber);
		pst.executeUpdate();
	}

	public void declineInvite(String phoneNumber, int groupId) throws SQLException {
		String sqlString = "DELETE FROM public.group_members WHERE group_id = ? AND phone_number = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		pst.setString(2, phoneNumber);
		pst.executeUpdate();
	}

	public ResultSet getDeceasedInfo(int groupId) throws SQLException {
		String sqlString = "SELECT * FROM public.deceased WHERE group_id = ?";
		PreparedStatement pst = databaseConnection.prepareStatement(sqlString);
		pst.setInt(1, groupId);
		return pst.executeQuery();
	}
}