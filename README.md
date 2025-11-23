# 🍕 PizzariaAPI

API REST para gerenciamento de usuários, produtos e compras de uma pizzaria, com autenticação JWT, integração com Mercado Pago e upload de imagens de produtos.  

Projeto criado como MVP real e também como portfólio profissional, focado em código limpo, segurança e boa observabilidade (logs).

---

## 🛠️ Tech Stack

- **Java 21**  
- **Spring Boot 3.5.7**  
  - Spring Web  
  - Spring Security (JWT)  
  - Spring Data JPA  
  - Validation  
- **Banco de dados:** MySQL 8+  
- **JWT (JSON Web Token)**  
- **Integração com Mercado Pago**  
- **Upload de imagens com MultipartFile**  
- **Swagger / OpenAPI**

---

## 📂 Estrutura do Projeto

```
src/main/java/com/pizzaria/demo
├── auth/
├── jwt/
├── springSecurity/
├── user/
├── product/
├── purchase/
├── itemProduct/
├── mercadoPago/
├── image/
└── config/
```

🔐 Segurança
- Login com JWT: /auth/login
- Enviar sempre: Authorization: Bearer <token>
- Controle via @PreAuthorize
- Soft delete para usuário e produto
Roles:
- USER
- ADMIN
- Segurança específica:
- Purchase só pode ser vista pelo dono ou admin:
```
@PreAuthorize("@purchaseSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
```

💳 Integração com Mercado Pago

Fluxo completo:
- Usuário chama POST /purchase/create
- API cria a Purchase
- API chama Mercado Pago gerando o link
- Usuário é redirecionado para o checkout
- Mercado Pago chama o webhook:

POST: 
/mercado-pago/webhook?secret=SEU_SECRET

DTO:
```
public record MercadoPagoWebhookDTO(
        Long id,
        String type,
        String action,
        Data data
) {
    public record Data(String id) {}
}

```


🖼️ Módulo de Imagens
Permite:
- Upload de imagem
- Atualização da imagem
- Remoção da imagem

Configuração:
```
pizzaria.images-dir=images
pizzaria.images.path=/caminho/para/pasta/images
```

📚 Endpoints

🔑 Autenticação (/auth)
- POST /auth/login

Público
```
{
  "email": "user@teste.com",
  "password": "123456"
}
```
- Retorna: JWT (string)
  
- POST /auth/create
Público
Cria usuário
Retorna: UserResponseDTO

👤 Usuários (/users)
-GET /users/{id} → Admin ou usuário dono
- GET /users → Apenas admin
- PUT /users/{id} → Admin ou dono
- DELETE /users/{id} → Admin ou dono (soft delete)

🍕 Produtos (/product)- GET /product/{id} → Autenticado
- GET /product → Autenticado
- POST /product → Admin
- PUT /product/{id} → Admin
- DELETE /product/{id} → Admin (soft delete)
- PATCH /product/{id}/status?active=truefalse → Admin

🖼️ Imagens (/images)- POST /images/product/{prodId} → Admin (upload)
- PATCH /images/product/{prodId} → Admin (atualizar)
- DELETE /images/product/{prodId} → Admin (remover)
🧾 Compras (/purchase)- POST /purchase/create → Roles: USER / ADMIN

```
{
  "itemProduct": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

- Retorna:

```
{
  "url": "https://www.mercadopago..."
}
```
- GET /purchase/ → USER / ADMIN
- GET /purchase/{id} → Dono ou admin
- PUT /purchase/{id}/cancel → Dono ou admin (soft delete)

💳 Webhook (/mercado-pago/webhook)

- POST /mercado-pago/webhook?secret=SEU_SECRET
- Validado com SECRET configurado
- Recebe notificações do Mercado Pago
- Processa status da purchase

📃 Swagger- Docs JSON: /v3/api-docs
- UI: /swagger-ui.html

🚀 Como rodar localmente 
- Requisitos:
- Java 21
- Maven
- MySQL 8+
- application.properties exemplo:

```
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/pizzariaapi
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

jwt.secret=MinhaChaveSecretaMaiorQue32Caracteres123456
jwt.expiration=86400000

webhook.secret=SEU_SECRET

pizzaria.images-dir=images
pizzaria.images.path=/caminho/para/pasta/images
```
Rodando:
```
mvn clean install
mvn spring-boot:run

http://localhost:8080
```

🧪 Testes

Incluem:
- 	PurchaseService
- 	ProductService
- 	ItemProductService
- 	UserService
- 	Regras de soft delete
- 	Lógica de preços no servidor

🧭 Próximos Passos
-	Front-end próprio (em andamento)
- 	Geração de NF-e
- 	Integração com transportadoras
- 	Métricas / Observabilidade
- 	Deploy com banco gerenciado

🚀 Deploy:
- O deploy será disponibilizado em breve.  
Como alternativa, o repositório contém instruções completas para rodar a API localmente.
O foco atual é finalizar integração de módulos e preparar o ambiente de produção.

👨‍💻 Autor
Fernando Prado
- GitHub: FernandoPPrado
- LinkedIn: fernando-prado21




  
