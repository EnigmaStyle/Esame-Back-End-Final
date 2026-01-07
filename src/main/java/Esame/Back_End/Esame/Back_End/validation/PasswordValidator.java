package Esame.Back_End.Esame.Back_End.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private static final String PASSWORD_PATTERN = 
        "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        boolean isValid = pattern.matcher(password).matches();
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character (@#$%^&+=!)"
            ).addConstraintViolation();
        }
        
        return isValid;
    }
}

