package com.biblioteca.api.service;

import com.biblioteca.api.model.Livro;
import com.biblioteca.api.repository.LivroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Page<Livro> listar(Pageable pageable) {
        return livroRepository.findAll(pageable);
    }

    public Livro buscarPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Livro não encontrado"
                ));
    }

    public Livro salvar(Livro livro) {
        return livroRepository.save(livro);
    }

    public Livro atualizar(Long id, Livro livro) {
        Livro livroExistente = buscarPorId(id);

        livroExistente.setTitulo(livro.getTitulo());
        livroExistente.setIsbn(livro.getIsbn());
        livroExistente.setAnoPublicacao(livro.getAnoPublicacao());
        livroExistente.setQuantidadePaginas(livro.getQuantidadePaginas());
        livroExistente.setAutor(livro.getAutor());
        livroExistente.setEditora(livro.getEditora());
        livroExistente.setCategorias(livro.getCategorias());

        return livroRepository.save(livroExistente);
    }

    public void excluir(Long id) {
        Livro livro = buscarPorId(id);
        livroRepository.delete(livro);
    }

    public Page<Livro> buscarPorTitulo(String titulo, Pageable pageable) {
        return livroRepository.findByTituloContainingIgnoreCase(titulo, pageable);
    }
}