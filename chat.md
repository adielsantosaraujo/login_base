Criar  uma change para implementar a funcionalidade de autenticação de usuário
O sistema trabalha com sessão
tabelas e campos iniciais 
usuarios : id, nome, email, senha, telefone
perfis : id, nome, descricao
permissoes : id, nome, descricao
usuario_rel_perfis : id, usuario_id, perfil_id, data_inicial, data_final
sessoes : id, usuario_id, data_inicio, data_fim, token, ip, dispositivo

campos padrão de auditoria em todas as tabelas : criado_em, criado_por, alterado_em, alterado_por

a tela de login deve ser criada em thymeleaf
dentro da pasta resources/templates/sistema/public e dentro dela criar o arquivo login.html
dentro da pasta resources/templates/sistema/seguro/ criar um arquivo index.html
o arquivo index deve ter apenas uma mensage "Seja bem vindo"

Toda as regras de negócio devem ser implementadas conforme padrão do springboot security


na tabela de usuario, o campo telefone passa a se chamar celular, e é unico, e deve ter 11 digitos que é o dd+numero
o usuario pode usar tambem usar o numero de celular para logar no sistema
