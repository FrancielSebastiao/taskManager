package com.fransebastiao.taskmanager.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
   
    @Override
    public void initialize(PasswordMatches constraintAnnotation) {}
   
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
        try {
            var passwordField = obj.getClass().getDeclaredMethod("password");
            var matchingPasswordField = obj.getClass().getDeclaredMethod("matchingPassword");
        
            String password = (String) passwordField.invoke(obj);
            String matchingPassword = (String) matchingPasswordField.invoke(obj);

            return password != null && password.equals(matchingPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
