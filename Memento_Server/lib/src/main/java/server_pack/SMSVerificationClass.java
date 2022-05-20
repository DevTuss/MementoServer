package server_pack;

import java.io.File;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSVerificationClass {

	public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
	public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
	public SMSVerificationClass() {
		// TODO Auto-generated constructor stub
		
		
	}
	
	public boolean sendCode(String code, String phoneNumber) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		System.out.println("Trying twilio with number: " + phoneNumber);
		String formNumber = "+46"+phoneNumber.substring(1);
		Message message = Message.creator(
				new PhoneNumber(formNumber),
				new PhoneNumber("+19893751841"),
				code).create();
		if(message.getSid().length()>0) {
			return true;
		} else 
			return false;
	}

}
