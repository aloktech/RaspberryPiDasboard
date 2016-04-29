package com.imos.pi.web.utils;

import javax.faces.bean.ManagedBean;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Alok
 */
@Setter @Getter @ManagedBean(name = "mailProperty")
public class MailProperty {

    private String mailSmtpHost, mailSmtpSslTrust, mailSmtpUser, mailSmtpPort, 
            mailSmtpAuth, to, from, subject, message;
    
    private boolean mailSmtpStarttlsEnable;
}
