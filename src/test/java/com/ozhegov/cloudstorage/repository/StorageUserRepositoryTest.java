package com.ozhegov.cloudstorage.repository;

import com.ozhegov.cloudstorage.entity.StorageUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class StorageUserRepositoryTest {
    @Autowired
    UserRepository repository;
    @Test
    public void testCreateFunction() {
        assertFalse(repository.existsByName("Nick"));
        addUserIntoDB();
        assertTrue(repository.existsByName("Nick"));
    }
    @Test
    public void testFindAll(){
        addUserIntoDB();
        List<StorageUser> storageUserList = repository.findAll();
        assertEquals(1, storageUserList.size());
    }
    private void addUserIntoDB() {
        try {
            StorageUser testStorageUser = new StorageUser();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            testStorageUser.setName("Nick");
            byte[] hash = md.digest("123456".getBytes());
            String hashedPassword = bytesToHex(hash);
            System.out.println(hashedPassword);
            testStorageUser.setPassword(hashedPassword);

            StorageUser savedStorageUser = repository.save(testStorageUser);
            System.out.println("Id of saved user: " + savedStorageUser.getId());
        }catch(NoSuchAlgorithmException e){
            System.out.println("АЛГОРИТМ НЕ НАШЕЛСЯ(((");
        }
    }
    private String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b: bytes){
            sb.append(String.format("%02x",b));
        }
        return sb.toString();
    }
}