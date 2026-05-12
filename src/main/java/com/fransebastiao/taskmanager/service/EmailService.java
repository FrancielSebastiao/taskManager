package com.fransebastiao.taskmanager.service;

public interface EmailService {
    void enviarEmailVerificacao(String destinatario, String nome, String linkVerificacao);
    void enviarEmailPasswordReset(String destinatario, String nome, String linkPasswordReset);
}
