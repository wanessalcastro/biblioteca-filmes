package com.exemplo.bibliotecafilmes.assembler;

import com.exemplo.bibliotecafilmes.controller.AvaliacaoController;
import com.exemplo.bibliotecafilmes.controller.FilmeController;
import com.exemplo.bibliotecafilmes.controller.UsuarioController;
import com.exemplo.bibliotecafilmes.dto.AvaliacaoResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AvaliacaoModelAssembler implements RepresentationModelAssembler<AvaliacaoResponseDTO, EntityModel<AvaliacaoResponseDTO>> {

    @Override
    public EntityModel<AvaliacaoResponseDTO> toModel(AvaliacaoResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(AvaliacaoController.class).buscarPorId(dto.id())).withSelfRel(),
                linkTo(methodOn(AvaliacaoController.class).atualizar(dto.id(), null)).withRel("update"),
                linkTo(methodOn(AvaliacaoController.class).remover(dto.id())).withRel("delete"),
                linkTo(AvaliacaoController.class).withRel("all"),
                linkTo(methodOn(FilmeController.class).buscarPorId(dto.filmeId())).withRel("filme"),
                linkTo(methodOn(UsuarioController.class).buscarPorId(dto.usuarioId())).withRel("usuario"));
    }
}
