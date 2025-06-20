package com.ozhegov.cloudstorage.repository;

import com.ozhegov.cloudstorage.model.entity.StorageUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DataJpaTest
class StorageUserRepositoryTest {
    @Mock
    UserRepository repository;
    @Test
    public void testCreateFunction() {
        assertFalse(repository.existsByName("Nick"));
        addUserIntoDB("Nick");
        assertTrue(repository.existsByName("Nick"));
        verify(repository).save(any(StorageUser.class));
    }
    @Test
    public void testFindAll(){
        addUserIntoDB("Andrew");
        addUserIntoDB("Jack");
        List<StorageUser> storageUserList = repository.findAll();
        assertEquals(2, storageUserList.size());
    }
    private void addUserIntoDB(String name) {
        StorageUser testStorageUser = new StorageUser();
        testStorageUser.setName(name);
        testStorageUser.setPassword("jd;l4/aihj3;lka1js5dfwesd31");
        testStorageUser.setRoles("ROLE_USER");

        StorageUser savedStorageUser = repository.save(testStorageUser);
        System.out.println("Id of saved user: " + savedStorageUser.getId());

    }
}