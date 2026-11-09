package ru.mirea.project.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.mirea.project.backend.repository.UserAccountRepository;

// Проверка контекста без внешней БД. Миграции проверяются на PostgreSQL.
@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
class ItOutsourceBackendApplicationTest {

    @MockitoBean
    private UserAccountRepository userAccountRepository;

    @Test
    void contextLoads() {
    }
}
