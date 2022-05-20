package Memento_Server;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.junit.Test;

import server_pack.DatabaseClass;

public class ServerTests {

	DatabaseClass databaseClass;
	/*@Test
	public void databaseInsertTest() {
		databaseClass = new DatabaseClass();
		try {
			databaseClass.register("0702204612");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
/*	@Test
	public void isRegisterd() {
		databaseClass = new DatabaseClass();
		try {
			System.out.println(databaseClass.isRegesterdUser("0704758350"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	/*@Test
	public void setPerson() throws SQLException {
		databaseClass = new DatabaseClass();
		System.out.println(databaseClass.setDeceased("Test testsson", "2005-05-24", "2022-06-11"));
	}*/
	
	@Test
	public void invite() throws SQLException {
		databaseClass = new DatabaseClass();
		//databaseClass.storePendingInvites("0702204613", 1, true);
		ResultSet resultSet = databaseClass.getPendingInvites("0702204613");
	//	if(resultSet.next()) {
			System.out.println(resultSet.getString(1));
			System.out.println(resultSet.getInt(2));
			System.out.println(resultSet.getBoolean(3));
	//	}
	}

}
