package com.gmail.alexei28.shortcut.microservices.post_service.dto;

import java.util.List;

public record PostDTO(String title, List<CommentDTO> comments) {
}
