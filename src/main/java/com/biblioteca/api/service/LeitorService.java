package com.biblioteca.api.service;

import com.biblioteca.api.model.Leitor;
import com.biblioteca.api.repository.LeitorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeitorService {

    private final LeitorRepository leitorRepository;

    public LeitorService(LeitorRepository leitorRepository) {
        this.leitorRepository = leitorRepository;
    }

    public Page<Leitor> listar(Pageable pageable) {
        return leitorRepository.findAll(pageable);
    }

    public Leitor buscarPorId(Long id) {
        return leitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Leitor não encontrado"
                ));
    }

    public Leitor salvar(Leitor leitor) {
        return leitorRepository.save(leitor);
    }

    public Leitor atualizar(Long id, Leitor leitor) {
        Leitor leitorExistente = buscarPorId(id);

        leitorExistente.setNome(leitor.getNome());
        leitorExistente.setEmail(leitor.getEmail());
        leitorExistente.setTelefone(leitor.getTelefone());

        return leitorRepository.save(leitorExistente);
    }

    public void excluir(Long id) {
        Leitor leitor = buscarPorId(id);
        leitorRepository.delete(leitor);
    }

    public Page<Leitor> buscarPorNome(String nome, Pageable pageable) {
        return leitorRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}