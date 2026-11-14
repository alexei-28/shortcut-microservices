package com.gmail.alexei28.shortcut.microservices.post_service.mapper;

import com.gmail.alexei28.shortcut.microservices.post_service.dto.PostDTO;
import com.gmail.alexei28.shortcut.microservices.post_service.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = CommentEntityMapper.class
)
public interface PostEntityMapper {
    @Mapping(target = "id", ignore = true)
    PostEntity toEntity(PostDTO postDTO);
}
