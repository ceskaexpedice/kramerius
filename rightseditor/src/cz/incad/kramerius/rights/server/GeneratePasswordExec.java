package cz.incad.kramerius.rights.server;

import java.text.MessageFormat;
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
import org.aplikator.server.descriptor.View;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import cz.incad.kramerius.rights.server.utils.GeneratePasswordUtils;
import cz.incad.kramerius.rights.server.utils.I18NUtils;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class GeneratePasswordExec implements Executable {

    public static final String EMAIL_REGEXP = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

    protected View userArr;
    protected Mailer mailer;


    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
        String result = null;
        try {
            PropertyDTO<String> email = ((Structure) Application.get()).user.EMAIL.clientClone(context);
            String emailAddres =  parameters.getClientContext().getCurrentRecord().getValue(email);
            if ((emailAddres != null) && (validation(emailAddres))) {

                PropertyDTO<String> pswd = ((Structure) Application.get()).user.PASSWORD.clientClone(context);
                PropertyDTO<String> loginname = ((Structure) Application.get()).user.LOGINNAME.clientClone(context);

                PropertyDTO<Integer> personalAdminDTO = ((Structure) Application.get()).user.PERSONAL_ADMIN.clientClone(context);

                String generated = GeneratePasswordUtils.generatePswd();

                RecordDTO currentRecord = parameters.getClientContext().getCurrentRecord();

                parameters.getClientContext().getCurrentRecord().setValue(pswd, PasswordDigest.messageDigest(generated));
                currentRecord.setNotForSave(personalAdminDTO, true);

                RecordContainer container = new RecordContainer();
                container.addRecord(getView().getViewDTO(context), currentRecord, currentRecord, Operation.UPDATE);
                AplikatorService service = context.getAplikatorService();
                service.execute(new ProcessRecords(container));

                GeneratePasswordUtils.sendGeneratedPasswordToMail(emailAddres, currentRecord.getValue(loginname), generated, mailer, context);

                String okResultString = I18NUtils.getLocalizedString("VygenerovatHeslo.ok.result", context);
                result = MessageFormat.format(okResultString, emailAddres);
            } else {
                String okResultString = I18NUtils.getLocalizedString("VygenerovatHeslo.notvalidmail", context);
                result = MessageFormat.format(okResultString, emailAddres);
            }

        } catch (Exception ex) {
            String failResult = I18NUtils.getLocalizedString("VygenerovatHeslo.fail.result", context);
            return new FunctionResult(MessageFormat.format(failResult, ex.getMessage()), false);
        }
        return new FunctionResult(result, true);
    }

    public Mailer getMailer() {
        return mailer;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public static boolean validation(String email) {
        boolean isValid = false;
        Pattern pattern = Pattern.compile(EMAIL_REGEXP, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }


    public View getView() {
        return userArr;
    }

    public void setArrangement(View userArr) {
        this.userArr = userArr;
    }

}
