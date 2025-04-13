package com.constructionhub.authentication;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "security.jwt.token.secret-key=chave-secreta-para-testes-123456",
    "security.jwt.token.expire-length=3600000",
    "security.jwt.refresh-token.expire-length=3600000"
})
public class AuthServiceApplicationTests {
    // ... seu código existente ...
}