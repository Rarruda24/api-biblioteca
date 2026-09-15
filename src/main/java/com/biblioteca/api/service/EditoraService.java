package com.biblioteca.api.service;

import com.biblioteca.api.model.Editora;
import com.biblioteca.api.repository.EditoraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EditoraService {

    private final EditoraRepository editoraRepository;

    public EditoraService(EditoraRepository editoraRepository) {
        this.editoraRepository = editoraRepository;
    }

    public Page<Editora> listar(Pageable pageable) {
        return editoraRepository.findAll(pageable);
    }

    public Editora buscarPorId(Long id) {
        return editoraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Editora não encontrada"
                ));
    }

    public Editora salvar(Editora editora) {
        return editoraRepository.save(editora);
    }

    public Editora atualizar(Long id, Editora editora) {
        Editora editoraExistente = buscarPorId(id);

        editoraExistente.setNome(editora.getNome());
        editoraExistente.setPais(editora.getPais());

        return editoraRepository.save(editoraExistente);
    }

    public void excluir(Long id) {
        Editora editora = buscarPorId(id);
        editoraRepository.delete(editora);
    }

    public Page<Editora> buscarPorNome(String nome, Pageable pageable) {
        return editoraRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}