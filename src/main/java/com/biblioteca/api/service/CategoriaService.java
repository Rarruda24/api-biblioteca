package com.biblioteca.api.service;

import com.biblioteca.api.model.Categoria;
import com.biblioteca.api.repository.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Page<Categoria> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable);
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                ));
    }

    public Categoria salvar(Categoria categoria) {

        if (categoriaRepository.existsByNome(categoria.getNome())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Categoria já cadastrada."
            );
        }

        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Long id, Categoria categoria) {

        Categoria categoriaExistente = buscarPorId(id);

        if (!categoriaExistente.getNome().equals(categoria.getNome())
                && categoriaRepository.existsByNome(categoria.getNome())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nome já utilizado por outra categoria."
            );
        }

        categoriaExistente.setNome(categoria.getNome());
        categoriaExistente.setDescricao(categoria.getDescricao());

        return categoriaRepository.save(categoriaExistente);
    }

    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        categoriaRepository.delete(categoria);
    }

    public Page<Categoria> buscarPorNome(
            String nome,
            Pageable pageable) {

        return categoriaRepository.findByNomeContainingIgnoreCase(
                nome,
                pageable
        );
    }
}