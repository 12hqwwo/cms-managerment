package com.project.WebAloTra.service;

import javax.mail.MessagingException;

import com.project.WebAloTra.dto.MailInfo;

public interface MailerService {
    void send(MailInfo mail) throws MessagingException;

    void queue(MailInfo mail);
}
