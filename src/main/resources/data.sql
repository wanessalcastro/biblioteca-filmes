-- Dados iniciais para testar a API (os ids são gerados em sequência a partir de 1)

INSERT INTO diretores (nome, nacionalidade, data_nascimento) VALUES
  ('Christopher Nolan', 'Britânico', '1970-07-30'),
  ('Fernando Meirelles', 'Brasileiro', '1955-11-09'),
  ('Greta Gerwig', 'Americana', '1983-08-04'),
  ('Denis Villeneuve', 'Canadense', '1967-10-03');

INSERT INTO generos (nome, descricao) VALUES
  ('Ficção Científica', 'Filmes baseados em avanços científicos e tecnológicos'),
  ('Drama', 'Narrativas centradas em conflitos emocionais'),
  ('Ação', 'Filmes com sequências de ação e aventura'),
  ('Comédia', 'Filmes com foco em humor'),
  ('Crime', 'Histórias envolvendo atividades criminosas');

INSERT INTO usuarios (nome, email, data_nascimento) VALUES
  ('Maria Silva', 'maria.silva@email.com', '1995-04-12'),
  ('João Souza', 'joao.souza@email.com', '1990-09-23'),
  ('Ana Pereira', 'ana.pereira@email.com', '2000-01-05');

INSERT INTO filmes (titulo, ano_lancamento, duracao_minutos, sinopse, classificacao, diretor_id) VALUES
  ('Interestelar', 2014, 169, 'Exploradores viajam através de um buraco de minhoca para salvar a humanidade.', 'DEZ', 1),
  ('A Origem', 2010, 148, 'Um ladrão invade sonhos para plantar uma ideia na mente de um empresário.', 'DOZE', 1),
  ('Cidade de Deus', 2002, 130, 'A ascensão do crime organizado numa favela do Rio de Janeiro.', 'DEZOITO', 2),
  ('Lady Bird', 2017, 94, 'O último ano do ensino médio de uma adolescente em Sacramento.', 'QUATORZE', 3),
  ('Duna', 2021, 155, 'Paul Atreides viaja ao planeta deserto Arrakis para proteger o futuro de sua família.', 'DOZE', 4);

INSERT INTO filme_genero (filme_id, genero_id) VALUES
  (1, 1), (1, 2),
  (2, 1), (2, 3),
  (3, 2), (3, 5),
  (4, 2), (4, 4),
  (5, 1), (5, 3);

INSERT INTO avaliacoes (nota, comentario, data_avaliacao, filme_id, usuario_id) VALUES
  (5, 'Obra-prima, trilha sonora incrível!', '2026-09-01', 1, 1),
  (4, 'Roteiro complexo, mas muito bom.', '2026-09-03', 2, 1),
  (5, 'Um clássico do cinema brasileiro.', '2026-09-10', 3, 2),
  (4, 'Sensível e divertido.', '2026-09-15', 4, 3),
  (5, 'Visualmente deslumbrante.', '2026-09-20', 5, 2),
  (3, 'Bom, mas achei longo demais.', '2026-09-21', 1, 3);

-- Chaves de API de DEMONSTRAÇÃO (só o hash SHA-256 é guardado). NÃO use em produção:
--   ADMIN   -> bf_demo_admin_0001      (usuário 1 - Maria Silva)
--   USUARIO -> bf_demo_usuario_0002    (usuário 2 - João Souza)
INSERT INTO api_keys (prefixo, chave_hash, descricao, nivel, ativa, criada_em, expira_em, usuario_id) VALUES
  ('bf_demo_ad', 'f5ab093875910f7814c6023531fd96f43cfafc79215f1946d2afb8fa973d8bf5', 'Chave de demonstração (ADMIN)', 'ADMIN', TRUE, CURRENT_TIMESTAMP, NULL, 1),
  ('bf_demo_us', '6c164aaf3ffa9548f719b4aba6acfd43b6f15f7a01413ca7d5e449b491242116', 'Chave de demonstração (USUARIO)', 'USUARIO', TRUE, CURRENT_TIMESTAMP, NULL, 2);
