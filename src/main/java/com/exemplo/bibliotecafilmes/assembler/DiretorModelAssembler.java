package com.exemplo.bibliotecafilmes.assembler;

import com.exemplo.bibliotecafilmes.controller.DiretorController;
import com.exemplo.bibliotecafilmes.controller.FilmeController;
import com.exemplo.bibliotecafilmes.dto.DiretorResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DiretorModelAssembler implements RepresentationModelAssembler<DiretorResponseDTO, EntityModel<DiretorResponseDTO>> {

    @Override
    public EntityModel<DiretorResponseDTO> toModel(DiretorResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(DiretorController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(DiretorController.class).atualizar(dto.id(), null)).withRel("update"),
                linkTo(methodOn(DiretorController.class).remover(dto.id())).withRel("delete"),
                linkTo(DiretorController.class).withRel("all"),
                linkTo(FilmeController.class).slash("diretor").slash(dto.id()).withRel("filmes"));
    }
}
