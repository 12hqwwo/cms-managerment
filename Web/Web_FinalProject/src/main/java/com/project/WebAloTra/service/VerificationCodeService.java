package com.project.WebAloTra.service;

import javax.mail.MessagingException;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.VerificationCode;

public interface VerificationCodeService {
    VerificationCode createVerificationCode(String email) throws MessagingException;

    Account verifyCode( String code);
}