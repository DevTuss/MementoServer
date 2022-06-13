package server_pack;

/**
 * ClientHandler
 * ï¿½rver frï¿½n Thread. Har hand om kommunikation mellan 
 * klient och server. 
 *  
 *  @author Caroline Engqvist (caen1500)
 *  @version 1.0
 *  @since 2020-01-11
 */
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.*;
//import org.apache.commons.codec.binary.Base64;

public class ClientHandler extends Thread{

	private Socket socket;
	private final String SERVER_DIR = new String ("\\xml");
	private DataInputStream in;
	private DataOutputStream out;
	private DatabaseClass databaseClass;
	

	// Konstruktor
	public ClientHandler(Socket s)
	{
		socket = s;
		databaseClass = new DatabaseClass();
		
	}
	public void run()
	{
		// Deklarerar instrï¿½m och utstrï¿½m
		boolean open = true;
		DataInputStream in = null;
		DataOutputStream out = null;
	
		try
		{
			// Skapar buffrade in- och utstr�mmar f�r kommunikation
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		}
		catch (IOException ioe)
		{
			System.out.println("Failed to create streams");
			return;
		}
		try
		{
			while(open) {
			// Tar emot val frï¿½n klienten
				String command = in.readUTF();
				System.out.println("Command '"+ command+ "' receved from " + socket.getRemoteSocketAddress());
				switch (command) {
				case "REG":
					registration(in, out);
					break;
				case "UPI":
					updateInvites(in, out);
					break;
				case "UPD":
					updateMedia(in, out);
					break;
				case "INV":
					invite(in, out);
					break;
				case "SEN":
					sendData(in);
					break;
				case "ACC":
					acceptInvite(in, out);
					break;
				case "DEC":
					declineInvite(in, out);
					break;

				default:
					break;
				}
						
			}
			
		}
	
		catch (IOException | SQLException ioe)
		{
			ioe.printStackTrace();
			System.out.println("No valid command received");
			return;
		}
	}
	
	private void registration(DataInputStream in, DataOutputStream out) throws IOException {
			
		String phonenumber = in.readUTF();
		String code = getRandomCode();
		boolean smsSent;
		smsSent = sendCode(code,phonenumber);
		if(!smsSent) {
			out.writeUTF("ERR");	// Error sending SMS
			out.flush();
			in.readUTF();
		} else if(smsSent) {
			if(in.readUTF().equals(code)) {
				try {
					databaseClass.register(phonenumber);
					out.writeUTF("REG"+phonenumber);	// If user entered the code correctly
					out.flush();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			} else {
				out.writeUTF("ERR"); 	// If user failed to enter the right code
				out.flush();
				in.readUTF();
			}
		}
	}
	
	private void updateInvites(DataInputStream in, DataOutputStream out) throws SQLException, IOException {
		String phoneNumber = in.readUTF();
		ResultSet inviteSet = databaseClass.getPendingInvites(phoneNumber);
		while (inviteSet.next()) {
			ResultSet groupInfo = databaseClass.getDeceasedInfo(inviteSet.getInt("group_id"));
			if (groupInfo.next()) {
				out.writeUTF(groupInfo.getString("name"));
				out.writeInt(groupInfo.getInt("group_id"));	
				out.writeUTF(groupInfo.getString("birth_date"));
				out.writeUTF(groupInfo.getString("deceased_date"));	
				out.writeBoolean(inviteSet.getBoolean("admin"));
			}
		}
		out.writeUTF("END");
		out.flush();
	}
	
	private void updateMedia(DataInputStream in, DataOutputStream out) throws IOException, SQLException {
		String phoneNumber = in.readUTF();
		int groupId = in.readInt();
		Timestamp oldTimestamp = Timestamp.valueOf(in.readUTF());
		Timestamp newTimestamp = new Timestamp(new java.util.Date().getTime());
		ResultSet updateSet = databaseClass.getUpdates(phoneNumber, groupId, oldTimestamp);
		while (updateSet.next()) {
			String type = updateSet.getString("data_type");
			out.writeUTF(type);
			out.writeInt(updateSet.getInt("data_size"));
			out.write(updateSet.getBytes("data"));
			if(type.equals("IMG") | type.equals("VID")) {
				String cate = updateSet.getString("category");
				out.writeUTF(cate);
			} 
		}
		out.writeUTF("END");
		out.writeUTF(newTimestamp.toString());
		out.flush();
	}
	
	private void invite(DataInputStream in, DataOutputStream out) throws IOException, SQLException {
		String groupName = null;
		String bDay = null;
		String dDay = null;
		String inviter = in.readUTF();
		String phoneNumber = in.readUTF();
		int groupId = in.readInt();
		boolean admin = in.readBoolean();
		if(groupId == 0) {
			groupName = in.readUTF();
			bDay = in.readUTF();
			dDay = in.readUTF();
		}
		
		if(databaseClass.isRegesterdUser(phoneNumber)) {
		
			groupId = databaseClass.membersOfGroup(inviter, phoneNumber, groupId, admin, groupName, bDay, dDay);
			out.writeInt(groupId);
			out.flush();
		} else {
			out.writeUTF("ERR");
			out.flush();
		}
		
	}
		
	private void sendData(DataInputStream in) throws IOException, SQLException {
		int length;
		String phonenumber = in.readUTF();
		int groupId = in.readInt();
		String dataType = in.readUTF();
		if(dataType.equals("wall")) {
			String media = in.readUTF(); 
			databaseClass.setData(phonenumber, groupId, dataType, media.getBytes(), null);
		} else {
			length = in.readInt();
			byte[] media = new byte[length];
			in.readFully(media);
			String category = in.readUTF();
			databaseClass.setData(phonenumber, groupId, dataType, media, category);
		}
		
	}
	
	private void acceptInvite(DataInputStream in, DataOutputStream out) throws IOException, SQLException {
		String phoneNumber = in.readUTF();
		int groupId = in.readInt();
		databaseClass.acceptInvite(phoneNumber, groupId);
	}
		
	private void declineInvite(DataInputStream in, DataOutputStream out) throws IOException, SQLException {
		String phoneNumber = in.readUTF();
		int groupId = in.readInt();
		databaseClass.declineInvite(phoneNumber, groupId);
	}
	
	
	
	private boolean sendCode(String code, String phoneNumber) {
		SMSVerificationClass smsVerificationClass = new SMSVerificationClass();
		return smsVerificationClass.sendCode(code, phoneNumber);	
		//return true;
	}
	
	private static String getRandomCode() {
		Random random = new Random();
		int code = random.nextInt(999999);
		return String.format("%06d", code);
	}
	
	public void close() {
		try
		{
			// Stï¿½nger ner streams och socket
			in.close();
			out.close();
			socket.close();
			System.out.println("Client from "+ socket.getRemoteSocketAddress()+" has disconnected");
		}
		catch(IOException ioe)
		{
			System.out.println("Could not disconnect.");
		}
	}
	
}
