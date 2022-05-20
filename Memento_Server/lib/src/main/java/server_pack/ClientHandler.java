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
				switch (command.substring(0, 3)) {
				case "REG":
					registration();
					break;
				case "UPD":
					updateInvites();
					updateMedia();
					break;
				case "INV":
					invite();
					break;
				case "SEN":
					sendData();
					break;
				case "ACC":
					acceptInvite();
					break;

				default:
					break;
				}
						
			}
			
		}
	
		catch (IOException | SQLException ioe)
		{
			System.out.println("No valid command received");
			//return;
		}
	}
	
	private void acceptInvite() throws IOException, SQLException {
		// TODO Auto-generated method stub
		Timestamp newTimestamp = new Timestamp(new java.util.Date().getTime());
		int groupId = in.readInt();
		databaseClass.getUpdates(groupId, null, newTimestamp);
	}
	private void updateInvites() throws SQLException, IOException {
		// TODO Auto-generated method stub
		String phoneNumber = in.readUTF();
		ResultSet inviteSet = databaseClass.getPendingInvites(phoneNumber);
		while (inviteSet.next()) {
			out.writeInt(inviteSet.getInt("GroupId"));
			out.writeUTF(inviteSet.getString("Name"));
			out.writeBoolean(inviteSet.getBoolean("Admin"));
		}
		out.writeUTF("END");
	}
	private void sendData() throws IOException {
		// TODO Auto-generated method stub
		String phonenumber = in.readUTF();
		int groupId = in.readInt();
		byte[] media = in.readAllBytes();// Base64.decodeBase64(in.readAllBytes());
		String category = in.readUTF();
		databaseClass.setData(phonenumber, groupId, media, category);
	}
	
	private void invite() throws IOException, SQLException {
		// TODO Auto-generated method stub
		String phoneNumber = in.readUTF();
		int groupId = in.readInt();
		String groupName = in.readUTF();
		boolean admin = in.readBoolean();
		databaseClass.storePendingInvites(phoneNumber, groupId, groupName, admin);
		
	}
	
	private void updateMedia() throws IOException, SQLException {
		// TODO Auto-generated method stub
		int groupId = in.readInt();
		Timestamp oldTimestamp = Timestamp.valueOf(in.readUTF());
		Timestamp newTimestamp = new Timestamp(new java.util.Date().getTime());
		ResultSet updateSet = databaseClass.getUpdates(groupId, oldTimestamp, newTimestamp);
		while (updateSet.next()) {
			out.write(updateSet.getBytes("Data"));
			out.writeUTF(updateSet.getString("Category"));
		}
		out.writeUTF(newTimestamp.toString());
	}
	
	public void registration() throws IOException {
		String phonenumber = in.readUTF();
		String code = getRandomCode();
		boolean smsSent;
		smsSent = sendCode(code,phonenumber);
		if(!smsSent) {
			out.writeUTF("ERR");	// Error sending SMS
			out.flush();
		} else if(smsSent) {
			if(in.readUTF().equals(code)) {
				out.writeUTF("REG"+phonenumber);	// If user entered the code correctly
				out.flush();
			} else {
				out.writeUTF("ERR"); 	// If user failed to enter the right code
				out.flush();
			}
		}
	}
	
	public boolean sendCode(String code, String phoneNumber) {
		//SMSVerificationClass smsVerificationClass = new SMSVerificationClass();
	//	return smsVerificationClass.sendCode(code, phoneNumber);	
		return false;
	}
	
	public static String getRandomCode() {
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

	


