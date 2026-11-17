package com.exemplo.bibliotecafilmes.assembler;

import com.exemplo.bibliotecafilmes.controller.FilmeController;
import com.exemplo.bibliotecafilmes.controller.GeneroController;
import com.exemplo.bibliotecafilmes.dto.GeneroResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GeneroModelAssembler implements RepresentationModelAssembler<GeneroResponseDTO, EntityModel<GeneroResponseDTO>> {

    @Override
    public EntityModel<GeneroResponseDTO> toModel(GeneroResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(GeneroController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(GeneroController.class).atualizar(dto.id(), null)).withRel("update"),
                linkTo(methodOn(GeneroController.class).remover(dto.id())).withRel("delete"),
                linkTo(GeneroController.class).withRel("all"),
                linkTo(FilmeController.class).slash("genero").slash(dto.id()).withRel("filmes"));
    }
}
