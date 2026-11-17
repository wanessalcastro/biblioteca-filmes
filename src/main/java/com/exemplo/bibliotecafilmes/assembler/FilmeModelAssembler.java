package com.exemplo.bibliotecafilmes.assembler;

import com.exemplo.bibliotecafilmes.controller.AvaliacaoController;
import com.exemplo.bibliotecafilmes.controller.DiretorController;
import com.exemplo.bibliotecafilmes.controller.FilmeController;
import com.exemplo.bibliotecafilmes.controller.GeneroController;
import com.exemplo.bibliotecafilmes.dto.FilmeResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FilmeModelAssembler implements RepresentationModelAssembler<FilmeResponseDTO, EntityModel<FilmeResponseDTO>> {

    @Override
    public EntityModel<FilmeResponseDTO> toModel(FilmeResponseDTO dto) {
        return EntityModel.of(dto, links(dto.id(), dto.diretorId()));
    }

    /** Links de um filme; compartilhados entre as versões 1 e 2 da representação. */
    public List<Link> links(Long id, Long diretorId) {
        return List.of(
                linkTo(methodOn(FilmeController.class).buscarPorId(id)).withSelfRel(),
                linkTo(methodOn(FilmeController.class).atualizar(id, null)).withRel("update"),
                linkTo(methodOn(FilmeController.class).remover(id)).withRel("delete"),
                linkTo(FilmeController.class).withRel("all"),
                linkTo(methodOn(DiretorController.class).buscarPorId(diretorId)).withRel("diretor"),
                linkTo(GeneroController.class).slash("filme").slash(id).withRel("generos"),
                linkTo(AvaliacaoController.class).slash("filme").slash(id).withRel("avaliacoes"));
    }
}
