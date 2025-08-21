package com.ozhegov.cloudstorage.repository;

import com.ozhegov.cloudstorage.model.entity.StorageUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StorageUserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void testCreateFunction() {
        assertFalse(repository.existsByName("Nick"));

        addUserIntoDB("Nick");

        assertTrue(repository.existsByName("Nick"));
    }

    @Test
    void testFindAll() {
        addUserIntoDB("Andrew");
        addUserIntoDB("Jack");

        List<StorageUser> storageUserList = repository.findAll();

        assertEquals(2, storageUserList.size());
        assertTrue(storageUserList.stream().anyMatch(u -> u.getName().equals("Andrew")));
        assertTrue(storageUserList.stream().anyMatch(u -> u.getName().equals("Jack")));
    }

    private void addUserIntoDB(String name) {
        StorageUser testStorageUser = new StorageUser();
        testStorageUser.setName(name);
        testStorageUser.setPassword("jd;l4/aihj3;lka1js5dfwesd31");
        testStorageUser.setRoles("ROLE_USER");
        repository.save(testStorageUser);
    }
}
