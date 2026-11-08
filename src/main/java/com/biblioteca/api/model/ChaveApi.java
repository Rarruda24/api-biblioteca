package com.biblioteca.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chaves_api", uniqueConstraints = @UniqueConstraint(name = "uk_chave_api_hash", columnNames = "hash_chave"))
public class ChaveApi {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String nome;
    @Column(name = "hash_chave", nullable = false, unique = true, length = 64)
    private String hashChave;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PlanoAcesso plano;
    @Column(nullable = false)
    private boolean ativa = true;
    @Column(nullable = false)
    private LocalDateTime criadaEm;
    public ChaveApi() {}
    @PrePersist public void prePersist() { if (criadaEm == null) criadaEm = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getHashChave() { return hashChave; }
    public void setHashChave(String hashChave) { this.hashChave = hashChave; }
    public PlanoAcesso getPlano() { return plano; }
    public void setPlano(PlanoAcesso plano) { this.plano = plano; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
