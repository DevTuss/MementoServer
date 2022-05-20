package server_pack;

/**
 * ClientHandler
 * �rver fr�n Thread. Har hand om kommunikation mellan 
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
import java.util.Random;
import java.io.*;
import org.apache.commons.codec.binary.Base64;

import com.twilio.base.Updater;


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
	}
	public void run()
	{
		// Deklarerar instr�m och utstr�m
		boolean open = true;
	
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
			// Tar emot val fr�n klienten
				String command = in.readUTF();
				//byte[] decodedBytes = Base64.decodeBase64(in.readAllBytes());
				//String command = decodedBytes.toString();
				System.out.println("Command '"+ command+ "' receved from " + socket.getRemoteSocketAddress());
			
				// Registration command
				if (command.substring(0, 3).equals("REG")) {	
					register();
					
				} else if(command.substring(0, 3).equals("INV")) { // add others to group
					inviteToGroup();
					// go through registerd users 
					// send a push notis to that user
					// accept or recline
					// some notification to user
				} else if(command.substring(0, 3).equals("UPD")) { // Update
					updateUserWithSharedContent();
					// go through registerd users 
					// send a push notis to that user
					// accept or recline
					// some notification to user
				} else if(command.substring(0, 3).equals("SEN")) {
					incommingDataToShare();
					System.out.println("send");
					//byte[] decodedBytes = Base64.decodeBase64(in.readAllBytes());
					//System.out.println("decodeBytes "+decodedBytes);
					// look up group members
					// store content in database
					// send information forward to group members
				} else if(command.substring(0, 3).equals("EXT")) {
					open = false;
					close();
				}
			
			}
			
		}
	
		catch (IOException ioe)
		{
			System.out.println("No valid command received");
			//return;
		}
	}
	private void incommingDataToShare() {
		// TODO Auto-generated method stub
		
	}
	private void updateUserWithSharedContent() {
		// TODO Auto-generated method stub
		
	}
	private void inviteToGroup() {
		// TODO Auto-generated method stub
		
	}
	public boolean sendCode(String code, String phoneNumber) {
		SMSVerificationClass smsVerificationClass = new SMSVerificationClass();
		return smsVerificationClass.sendCode(code, phoneNumber);		
	}
	
	public void register() throws IOException {
		String phoneNumber = in.readUTF();
		String code = getRandomCode();
		boolean smsSent = false;
		smsSent = sendCode(code,phoneNumber);
		if(!smsSent) {
			out.writeUTF("ERR");	// Error sending SMS
			out.flush();
		} else if(smsSent) {
			if(in.readUTF().equals(code)) {
				out.writeUTF("REG"+phoneNumber);	// If user entered the code correctly
				out.flush();
				databaseClass.saveRegisterdUser(phoneNumber);
			} else {
				out.writeUTF("ERR"); 	// If user failed to enter the right code
				out.flush();
			}
		}
	}
	
	public static String getRandomCode() {
		Random random = new Random();
		int code = random.nextInt(999999);
		
		return String.format("%06d", code);
	}
	
	public void close() {
		try
		{
			// St�nger ner streams och socket
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

	


