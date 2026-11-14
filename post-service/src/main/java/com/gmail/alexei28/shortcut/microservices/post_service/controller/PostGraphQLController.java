package com.gmail.alexei28.shortcut.microservices.post_service.controller;

import com.gmail.alexei28.shortcut.microservices.post_service.entity.PostEntity;
import com.gmail.alexei28.shortcut.microservices.post_service.service.PostService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/*
    GraphQL не является HTTP-протоколом.
    GraphQL — это query language + execution model.
    Он может работать поверх HTTP. Также GraphQL может использовать WebSocket для subscriptions.

    В отличие от REST, где может быть:
      GET /users
      GET /users/{id}
      POST /users
      GET /posts
      GET /posts/{id}

    GraphQL обычно имеет одну точку входа:
      POST /graphql

    Какую конкретно информацию вы запрашиваете (пользователей, посты, настройки), сервер определяет не по адресу URL,
    а по тексту (передаваемому query или mutation) внутри тела этого единственного запроса.
    Важно:
    Один HTTP request не означает, что внутри сервера будет один database request.

    Пример типичного запроса к GraphQL endpoint.
    Для примера предположим, что мы отправляем HTTP POST запрос на адрес [https://api.example.com/graphql](https://api.example.com/graphql).
    1. Тело запроса (JSON)
        GraphQL-запрос передается в теле запроса в поле query.
        Мы можем запросить только те поля, которые нам действительно нужны (в данном случае — id поста и title поста):
    {
      "query": "query { postById(id: 1052) { id title } }"
    }

     2. Пример ответа от сервера (JSON)
     Сервер возвращает ровно ту структуру данных, которую вы запросили:
     {
      "data": {
        "postById": {
          "id": "1052",
          "title": "Title#2"
        }
      }
    }
*/
@Controller
public class PostGraphQLController {

    private final PostService postService;

    public PostGraphQLController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public PostEntity postById(@Argument Long id) {
        return postService.getPostById(id);
    }
}