package com.biblioteca.api.model;

public enum PlanoAcesso {
    BASICO(30), PROFISSIONAL(100), PREMIUM(300);
    private final int limitePorMinuto;
    PlanoAcesso(int limitePorMinuto) { this.limitePorMinuto = limitePorMinuto; }
    public int getLimitePorMinuto() { return limitePorMinuto; }
}
