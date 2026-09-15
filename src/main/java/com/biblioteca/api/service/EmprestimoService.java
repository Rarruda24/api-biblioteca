package com.biblioteca.api.service;

import com.biblioteca.api.model.Emprestimo;
import com.biblioteca.api.model.StatusEmprestimo;
import com.biblioteca.api.repository.EmprestimoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository) {
        this.emprestimoRepository = emprestimoRepository;
    }

    public Page<Emprestimo> listar(Pageable pageable) {
        return emprestimoRepository.findAll(pageable);
    }

    public Emprestimo buscarPorId(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empréstimo não encontrado"
                ));
    }

    public Emprestimo salvar(Emprestimo emprestimo) {
        return emprestimoRepository.save(emprestimo);
    }

    public Emprestimo atualizar(Long id, Emprestimo emprestimo) {
        Emprestimo emprestimoExistente = buscarPorId(id);

        emprestimoExistente.setDataEmprestimo(emprestimo.getDataEmprestimo());
        emprestimoExistente.setDataPrevistaDevolucao(emprestimo.getDataPrevistaDevolucao());
        emprestimoExistente.setDataDevolucao(emprestimo.getDataDevolucao());
        emprestimoExistente.setStatus(emprestimo.getStatus());
        emprestimoExistente.setLeitor(emprestimo.getLeitor());
        emprestimoExistente.setLivro(emprestimo.getLivro());

        return emprestimoRepository.save(emprestimoExistente);
    }

    public void excluir(Long id) {
        Emprestimo emprestimo = buscarPorId(id);
        emprestimoRepository.delete(emprestimo);
    }

    public Page<Emprestimo> buscarPorStatus(
            StatusEmprestimo status,
            Pageable pageable) {

        return emprestimoRepository.findByStatus(status, pageable);
    }
}