package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DefaultProperty(value = "icon")
public class PhoneValidator extends ValidatorBase {
    public static boolean valid = false;
    /*
     * global-phone-number = ["+"] 1*( DIGIT / written-sep )
     * written-sep         = ("-"/".")
     */
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN =
            Pattern.compile("[\\+]?[0-9.-]+");
    private static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.length() < 8) {
            return false;
        }

        Matcher match = GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber);
        return match.matches();
    }

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            if (!isGlobalPhoneNumber(((TextInputControl)srcControl.get()).getText())) {
                hasErrors.set(true);
                valid = false;
            } else {
                hasErrors.set(false);
                valid = true;
            }
        }
    }
}
