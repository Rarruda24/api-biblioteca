package com.biblioteca.api.assembler;

import com.biblioteca.api.controller.AutorController;
import com.biblioteca.api.model.Autor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class AutorModelAssembler
        implements RepresentationModelAssembler<Autor, EntityModel<Autor>> {

    @Override
    public EntityModel<Autor> toModel(Autor autor) {

        return EntityModel.of(
                autor,

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(AutorController.class)
                                        .buscarPorId(autor.getId())
                        )
                        .withSelfRel(),

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(AutorController.class)
                                        .excluir(autor.getId())
                        )
                        .withRel("delete")
        );
    }
}