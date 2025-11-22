# 🍕 PizzariaAPI — Spring Boot + JWT + Mercado Pago  
API completa para um MVP de sistema de pedidos de pizzaria, com autenticação JWT, integração com Mercado Pago, upload de imagens, CRUDs essenciais, logs profissionais e documentação via Swagger.

---

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Web (MVC)**
- **Spring Security + JWT**
- **Spring Data JPA**
- **MySQL**
- **Lombok**
- **Mercado Pago Integration (Checkout Pro + Webhook)**
- **Upload de Imagens via MultipartFile**
- **Swagger (springdoc-openapi 2.6.0)**

---

## 📌 Funcionalidades

### 🔐 Autenticação
- Login via JWT  
- Proteção de endpoints com `@PreAuthorize`  
- `AuthTokenFilter` validando tokens em cada request  
- Segurança configurada para API REST (stateless)

### 👤 Usuários
- Cadastro e login  
- Soft-delete  
- Busca por ID  
- Logs detalhados

### 🍕 Produtos
- CRUD completo  
- Upload de imagens  
- Armazenamento local (`/images/product-<id>.png`)  

### 🛒 Compras (Purchase)
- Criação de compras  
- Cálculo automático de total  
- Associação de itens (ItemProduct)  
- Validação de preços direto do banco (não vem do front)  
- Controle de estoque  
- Consulta por usuário  
- Logs completos (info, warn, debug)

### 💳 Mercado Pago
Fluxo completo:
1. Usuário cria a compra  
2. API cria a **Preference** no Mercado Pago  
3. Recebe o **payment_link**  
4. Webhook atualiza o status da compra automaticamente  
5. Idempotência garantida  
6. Armazena o `paymentId`  

Estados suportados:
- `PENDING`
- `APPROVED`
- `REJECTED`
- `CANCELED`
- `COMPLETED`

### 🖼️ Sistema de Imagens
- Upload com `MultipartFile`  
- Salvamento local  
- URL automática no Produto  
- Suporta atualização da imagem  

### 📦 Estrutura de Logs
- `INFO`, `WARN`, `ERROR`, `DEBUG` separados por cores  
- Logs em todos os serviços  
- Sem exposição de stacktrace para o cliente  
- `BasicErrorController` configurado

---

## 📁 Estrutura do Projeto

src/main/java/com/pizzaria/demo
├── auth/
├── jwt/
├── springSecurity/
├── user/
├── product/
├── purchase/
├── itemproduct/
├── mercadopago/
├── image/
└── config/

---

## 📄 Documentação da API (Swagger)

Acesse: http://localhost:8080/swagger-ui.html


### Segurança no Swagger

Após fazer login na rota `/auth/login`, copie o token JWT e clique em **Authorize**:

Bearer SEU_TOKEN_AQUI


A partir disso, todas as rotas protegidas funcionam no Swagger.

---

## 🔧 Como rodar o projeto

### 1. Configurar o banco
Crie um banco MySQL: pizzaria_db

### 2. Configurar o `application.properties`

spring.datasource.url=jdbc:mysql://localhost:3306/pizzaria_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

Mercado Pago (suas credenciais)

mercadopago.access-token=SEU_TOKEN_MP
mercadopago.notification-url=https://sua-url/webhook

### 3. Rodar com Maven

---

## 🔐 Segurança (Resumo)

O projeto usa:
- JWT assinado  
- Filtro: `AuthTokenFilter`  
- Stateless session  
- Swagger liberado  
- Webhook do Mercado Pago liberado  
- Tudo mais autenticado via Bearer Token  

---

## 💳 Fluxo de Pagamento (Mercado Pago)

1. Front chama `/purchase/create`  
2. API salva a compra (status: PENDING)  
3. API cria Preference no Mercado Pago  
4. Devolve o `payment_link` para o front  
5. Usuário paga  
6. Mercado Pago envia **webhook**  
7. API atualiza o status da compra

Webhook público: POST /mercado-pago/webhook

---

## 🖼️ Upload de Imagens

Rota: POST /product/{id}/image

Internamente o serviço salva como: /images/product-<id>.png


E atualiza o campo `imageUrl` no Produto.

---

## 🧪 Testes

O projeto inclui:
- Testes básicos com Spring Boot Test  
- Testes para serviços  
- Testes para camada de segurança  
- Validação de regras de negócio  

---

## 🛠️ TODO (MVP)

- Redução de estoque ao finalizar compra  
- Página de sucesso/erro/pending no front  
- Deploy (Fly.io ou Railway)  
- Integração com front React (em desenvolvimento)

---

## 🤝 Contribuição

1. Fork  
2. Crie uma branch feature  
3. Commit  
4. Pull Request  

---

## 📄 Licença

MIT.  
Use, modifique e aprenda livremente.








