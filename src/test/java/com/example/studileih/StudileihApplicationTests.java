package com.example.studileih;

import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.*;
import javafx.application.Application;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;


class StudileihApplicationTests {



	@Test
	public void whenSenderAndReceiverGetDeleted_thenAlsoDeleteTheMessages() {
//create Test Data
		User harald = new User("HaraldTest", "grg.kro@gmail.com", "2345");
		User hartmut = new User("HartmuTest", "georgkromer@pm.me", "5432");

//		userService.saveOrUpdateUser(harald);
//		userService.saveOrUpdateUser(hartmut);

		Message fromHaraldToHartmut = new Message("TestEinsZwoEinsZwo", "Testtesttestte", "11.11.2020 11:11:11", harald, hartmut);
		Message fromHaraldToHartmut2 = new Message("TestEinsZwoEinsZwo2", "Testtesttestte2", "12.11.2020 11:11:11", harald, hartmut);
		Message fromHartmutToHarald = new Message("TestEinsZwoEinsZwo2", "Testtesttestte2", "12.11.2020 11:11:11", hartmut, harald);
		List<Message> messagesHarald = new ArrayList<>();
		List<Message> messagesHartmut = new ArrayList<>();
		messagesHarald.add(fromHaraldToHartmut);
		messagesHarald.add(fromHaraldToHartmut);
		messagesHarald.add(fromHartmutToHarald);
		messagesHartmut.add(fromHaraldToHartmut);
		messagesHartmut.add(fromHaraldToHartmut2);
		messagesHartmut.add(fromHartmutToHarald);
		harald.setSentMessages(messagesHarald);
		hartmut.setReceivedMessages(messagesHartmut);

// test...


	}

	@Test
	public void simplyTestThatJasyptEncryptionWorks() {
		//https://www.baeldung.com/jasypt
		//To perform encryption and decryption using a very simple algorithm, we can use a BasicTextEncryptor class from the Jasypt library:
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		String privateData = "secret-data";
		textEncryptor.setPasswordCharArray("some-random-data".toCharArray());

		// Then we can use an encrypt() method to encrypt the plain text:
		String myEncryptedText = textEncryptor.encrypt(privateData);
        assertNotSame(privateData, myEncryptedText);

        String plainText = textEncryptor.decrypt(myEncryptedText);
		// Test passes when plainText = "secret-data"
        assertEquals(plainText, privateData);

        // Ideally, we want to encrypt the password without a way to decrypt it. When the user tries to log into our service, we encrypt his password and compare it with the encrypted password that is stored in the database. That way we do not need to operate on plain text password.
		String password = "secret-pass";
		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
		String encryptedPassword = passwordEncryptor.encryptPassword(password);

		boolean result = passwordEncryptor.checkPassword("secret-pass", encryptedPassword);

		assertTrue(result);

		//In Jasypt we can use strong encryption by using a StandardPBEStringEncryptor class and customize it using a setAlgorithm() method: (no need to install anything since JDK 8)
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		String privateData2 = "secret-data";
		encryptor.setPassword("some-random-passwprd");
		encryptor.setAlgorithm("PBEWithMD5AndTripleDES");

		String encryptedText = encryptor.encrypt(privateData2);
		// System.out.println(encryptedText);
		assertNotSame(privateData2, encryptedText);

		String plainText2 = encryptor.decrypt(encryptedText);
		assertEquals(plainText2, privateData2);
	}




}
