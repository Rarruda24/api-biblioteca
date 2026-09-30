package com.biblioteca.api.assembler;

import com.biblioteca.api.controller.LivroController;
import com.biblioteca.api.model.Livro;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class LivroModelAssembler
        implements RepresentationModelAssembler<Livro, EntityModel<Livro>> {

    @Override
    public EntityModel<Livro> toModel(Livro livro) {

        return EntityModel.of(
                livro,

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(LivroController.class)
                                        .buscarPorId(livro.getId())
                        )
                        .withSelfRel(),

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(LivroController.class)
                                        .excluir(livro.getId())
                        )
                        .withRel("delete")
        );
    }
}