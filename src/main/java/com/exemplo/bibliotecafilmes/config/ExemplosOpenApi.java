package com.exemplo.bibliotecafilmes.config;

/** Exemplos reutilizados nas anotações {@code @ExampleObject} dos controllers. */
public final class ExemplosOpenApi {

    private ExemplosOpenApi() {
    }

    public static final String ERRO_404 = """
            {
              "timestamp": "2026-10-13T20:15:30",
              "status": 404,
              "erro": "Not Found",
              "mensagem": "Recurso não encontrado(a) com id 999",
              "caminho": "/recursos/999",
              "campos": null
            }""";

    public static final String ERRO_400 = """
            {
              "timestamp": "2026-10-13T20:15:30",
              "status": 400,
              "erro": "Bad Request",
              "mensagem": "Dados de entrada inválidos",
              "caminho": "/recursos",
              "campos": [
                { "campo": "nome", "mensagem": "não deve estar em branco" }
              ]
            }""";

    public static final String ERRO_409 = """
            {
              "timestamp": "2026-10-13T20:15:30",
              "status": 409,
              "erro": "Conflict",
              "mensagem": "Já existe um registro com este valor",
              "caminho": "/recursos",
              "campos": null
            }""";

    public static final String FILME_V1 = """
            {
              "id": 1,
              "titulo": "Interestelar",
              "anoLancamento": 2014,
              "duracaoMinutos": 169,
              "sinopse": "Exploradores viajam através de um buraco de minhoca para salvar a humanidade.",
              "classificacao": "DEZ",
              "diretorId": 1,
              "diretorNome": "Christopher Nolan",
              "generos": [
                { "id": 1, "nome": "Ficção Científica", "descricao": "Filmes baseados em avanços científicos e tecnológicos" },
                { "id": 2, "nome": "Drama", "descricao": "Narrativas centradas em conflitos emocionais" }
              ],
              "_links": { "self": { "href": "http://localhost:8080/filmes/1" } }
            }""";

    public static final String FILME_V2 = """
            {
              "id": 1,
              "titulo": "Interestelar",
              "anoLancamento": 2014,
              "duracaoMinutos": 169,
              "duracaoFormatada": "2h49min",
              "sinopse": "Exploradores viajam através de um buraco de minhoca para salvar a humanidade.",
              "classificacao": "DEZ",
              "diretor": { "id": 1, "nome": "Christopher Nolan" },
              "generos": ["Ficção Científica", "Drama"],
              "mediaAvaliacoes": 4.0,
              "totalAvaliacoes": 2,
              "_links": { "self": { "href": "http://localhost:8080/filmes/1" } }
            }""";
}
