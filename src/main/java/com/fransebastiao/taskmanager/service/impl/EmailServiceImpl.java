package com.fransebastiao.taskmanager.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fransebastiao.taskmanager.exception.custom.EmailException;
import com.fransebastiao.taskmanager.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void enviarEmailVerificacao(String destinatario, String nome, String linkVerificacao) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinatario);
            message.setSubject("Confirme a sua conta");
            message.setText(construirMensagem(nome, linkVerificacao));
            mailSender.send(message);
            log.info("Email de verificação enviado para: {}", destinatario);
        } catch (MailException e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new EmailException("Não foi possível enviar o email de verificação", e);
        }
    }

    public void enviarEmailPasswordReset(String destinatario, String nome, String linkPasswordReset) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinatario);
            message.setSubject("Redefinir Senha");
            message.setText(construirPasswordResetMensagem(nome, linkPasswordReset));
            mailSender.send(message);
            log.info("Email de redefinição de senha enviado para: {}", destinatario);
        } catch (MailException e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new EmailException("Não foi possível enviar o email de redefinição de senha", e);
        }
    }

    private String construirMensagem(String nome, String link) {
        return """
                Olá, %s!
                
                Obrigado por criar a sua conta. Por favor confirme o seu endereço de email clicando no link abaixo:
                
                %s
                
                Este link expira em 24 horas.
                
                Se não criou esta conta, pode ignorar este email.
                """.formatted(nome, link);
    }

    private String construirPasswordResetMensagem(String nome, String link) {
        return """
                Olá, %s!
                
                Recebemos uma solicitação para redefinir sua senha. Clique no link abaixo para escolher uma nova:
                
                %s
                
                Este link expira em 15 minutos.
                
                Se você não solicitou a redefinição de senha, pode ignorar este email com segurança - sua senha não será alterada.
                """.formatted(nome, link);
    }
}