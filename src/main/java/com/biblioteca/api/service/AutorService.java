package com.biblioteca.api.service;

import com.biblioteca.api.model.Autor;
import com.biblioteca.api.repository.AutorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public Page<Autor> listar(Pageable pageable) {
        return autorRepository.findAll(pageable);
    }

    public Autor buscarPorId(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Autor não encontrado"
                ));
    }

    public Autor salvar(Autor autor) {
        return autorRepository.save(autor);
    }

    public Autor atualizar(Long id, Autor autor) {
        Autor autorExistente = buscarPorId(id);

        autorExistente.setNome(autor.getNome());
        autorExistente.setNacionalidade(autor.getNacionalidade());
        autorExistente.setDataNascimento(autor.getDataNascimento());

        return autorRepository.save(autorExistente);
    }

    public void excluir(Long id) {
        Autor autor = buscarPorId(id);
        autorRepository.delete(autor);
    }

    public Page<Autor> buscarPorNome(String nome, Pageable pageable) {
        return autorRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}