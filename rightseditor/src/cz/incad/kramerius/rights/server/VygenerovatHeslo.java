package cz.incad.kramerius.rights.server;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.aplikator.client.data.Operation;
import org.aplikator.client.data.RecordContainer;
import org.aplikator.client.data.RecordDTO;
import org.aplikator.client.descriptor.PropertyDTO;
import org.aplikator.client.rpc.AplikatorService;
import org.aplikator.client.rpc.impl.ProcessRecords;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import cz.incad.kramerius.rights.server.arragements.UserArrangement;
import cz.incad.kramerius.rights.server.utils.GeneratePasswordUtils;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class VygenerovatHeslo  implements Executable {
    
	public static final String EMAIL_REGEXP = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	
	private UserArrangement userArr;
	private Mailer mailer;
	
	@Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
        String result = null;
        try{
        	PropertyDTO email = ((Structure) Application.get()).user.EMAIL.clientClone(context);
        	String emailAddres = parameters.getClientContext().getCurrentRecord().getStringValue(email);
        	if ((emailAddres != null) && (validation(emailAddres))) {

        		PropertyDTO pswd = ((Structure) Application.get()).user.PASSWORD.clientClone(context);
        		PropertyDTO loginname = ((Structure) Application.get()).user.LOGINNAME.clientClone(context);
        		
        		PropertyDTO personalAdminDTO = ((Structure) Application.get()).user.PERSONAL_ADMIN.clientClone(context);

            	String generated = GeneratePasswordUtils.generatePswd();
            	
            	RecordDTO currentRecord = parameters.getClientContext().getCurrentRecord();

            	parameters.getClientContext().getCurrentRecord().setValue(pswd, PasswordDigest.messageDigest(generated));
        		currentRecord.setNotForSave(personalAdminDTO, true);


            	RecordContainer container = new RecordContainer();
            	container.addRecord(this.userArr.getArrangementDTO(context), currentRecord, currentRecord, Operation.UPDATE);
            	AplikatorService service = context.getAplikatorService();
            	service.execute(new ProcessRecords(container));
            
            	GeneratePasswordUtils.sendGeneratedPasswordToMail(emailAddres, currentRecord.getStringValue(loginname),generated, mailer);

            	result = "Heslo odeslano na adresu: "+emailAddres;
        	} else {
            	result = "Nevalidni adresa: "+emailAddres;
        	}
        	
        }catch (Exception ex){
            return new FunctionResult("Chyba: "+ex, false);
        }
    	return new FunctionResult(result, true);
    }



	public UserArrangement getUserArr() {
		return userArr;
	}



	public void setUserArr(UserArrangement userArr) {
		this.userArr = userArr;
	}



	public Mailer getMailer() {
		return mailer;
	}



	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}

	public static boolean validation(String email) {
		boolean isValid = false;
		Pattern pattern = Pattern.compile(EMAIL_REGEXP,Pattern.CASE_INSENSITIVE);  
		Matcher matcher = pattern.matcher(email);  
		if(matcher.matches()){  
			isValid = true;  
		}  
		return isValid;  
	}
	public static void main(String[] args) {
		
		System.out.println(validation("stastny@gmail.com"));
	}
	
}