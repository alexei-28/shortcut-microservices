package com.gmail.alexei28.shortcut.microservices.post_service.mapper;

import com.gmail.alexei28.shortcut.microservices.post_service.dto.CommentDTO;
import com.gmail.alexei28.shortcut.microservices.post_service.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    CommentEntity toEntity(CommentDTO dto);
}