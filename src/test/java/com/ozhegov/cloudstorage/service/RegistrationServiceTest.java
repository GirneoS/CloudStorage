package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.model.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_${random.value};DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RegistrationServiceTest {
    @Autowired
    RegistrationService service;
    @Autowired
    UserRepository repository;
    @BeforeEach
    public void setup(){
        repository.deleteAll();
    }
    @Test
    public void testRegistrationUserWithUniqueName(){
        StorageUser user1 = new StorageUser();
        user1.setName("James");
        user1.setPassword(":olifj43er7wijh6lfa3424sdf/");
        user1.setRoles("ROLE_USER");
        StorageUser user2 = new StorageUser();
        user2.setName("Jason");
        user2.setPassword("I1/£4bq4J4f/foiuH77");
        user2.setRoles("ROLE_ADMIN");

        repository.save(user1);
        repository.save(user2);

        assertEquals(2, repository.findAll().size());

        assertEquals("ROLE_USER", repository.findByName("James").orElseGet(StorageUser::new).getRoles());
        assertEquals("ROLE_ADMIN", repository.findByName("Jason").orElseGet(StorageUser::new).getRoles());
    }
    @Test
    public void testRegistrationUserWithDuplicatingName(){
        StorageUser user1 = new StorageUser();
        user1.setName("James");
        user1.setPassword(":olifj43er7wijh6lfa3424sdf/");
        user1.setRoles("ROLE_USER");
        StorageUser user2 = new StorageUser();
        user2.setName("James");
        user2.setPassword("I1/£4bq4J4f/foiuH77");
        user2.setRoles("ROLE_ADMIN");
    }
}