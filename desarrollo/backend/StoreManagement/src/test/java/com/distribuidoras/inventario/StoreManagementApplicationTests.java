package com.distribuidoras.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class StoreManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
