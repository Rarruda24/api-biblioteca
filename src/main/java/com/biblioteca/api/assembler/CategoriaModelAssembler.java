package com.biblioteca.api.assembler;

import com.biblioteca.api.controller.CategoriaController;
import com.biblioteca.api.model.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<Categoria, EntityModel<Categoria>> {

    @Override
    public EntityModel<Categoria> toModel(Categoria categoria) {
        var selfVersionado = VersionedLinks.forCurrentRequest("categorias", categoria.getId(), "self");
        var deleteVersionado = VersionedLinks.forCurrentRequest("categorias", categoria.getId(), "delete");
        var updateVersionado = VersionedLinks.forCurrentRequest("categorias", categoria.getId(), "update");
        if (selfVersionado != null && deleteVersionado != null && updateVersionado != null)
            return EntityModel.of(categoria, selfVersionado, updateVersionado, deleteVersionado);

        return EntityModel.of(
                categoria,

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(CategoriaController.class)
                                        .buscarPorId(categoria.getId())
                        )
                        .withSelfRel(),

                WebMvcLinkBuilder
                        .linkTo(
                                WebMvcLinkBuilder
                                        .methodOn(CategoriaController.class)
                                        .excluir(categoria.getId())
                        )
                        .withRel("delete")
        );
    }
}
