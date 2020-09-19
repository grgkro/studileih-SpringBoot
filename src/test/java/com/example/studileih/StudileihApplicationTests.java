package com.example.studileih;

import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.ProductRepository;
import com.example.studileih.Service.ProductService;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;

//import javafx.application.Application;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
//@ContextConfiguration // https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/test-method.html
@SpringBootTest
class StudileihApplicationTests {

	@Autowired
	private MockMvc mvc;

	// Repository to be mocked as unavailable for unit test.
//	The @Mock annotation creates a mock implementation for the class it is annotated with.
	@Mock
	private ProductRepository productRepository;

	//	@InjectMocks also creates the mock implementation, additionally injects the dependent mocks that are marked with the annotations @Mock into it.
	@InjectMocks
	private ProductService productService;

	@Test
	public void contextLoads() {
	}

	@Test
	public void whenUsingS3_thenFileLocationEqualsStringInApplicationProperties() {

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

	@Test
	public void whenInputIsValid_thenReturn200() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				0L,
				"2598342143.66",
				"2598342144",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 200);
	}

	@Test
	public void whenDescriptionIsEmpty_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"   ",
				"title",
				"testString",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenTitleIsEmpty_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"description",
				" ",
				"testString",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenDescriptionContainsForbiddenChars_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"description\\",
				"title",
				"testString",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenTitleContainsForbiddenChars_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"description",
				"#title",
				"testString",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenTitleContainsForbiddenChars2_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"description",
				"!title",
				"testString",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenCategoryContainsForbiddenChars_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"description",
				"title",
				"testString{}",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenCategoryContainsNoForbiddenChars_thenReturn200() {
		ResponseEntity response = productService.validateInput( null,
				"description",
				"title",
				"testString xBox360 .-_öäüßasÖÄÜ.360",
				null,
				0L,
				null,
				null,
				null,
				null,
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 200);
	}

	@Test
	public void whenPriceIsNegative_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				-1L,
				null,
				"2598342144",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenPriceIsTooHigh_thenReturn200() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				101L,
				null,
				"2598342144",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
	}

	@Test
	public void whenStartDateIsBeforeCurrentDate_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				0L,
				"598342143.66",
				"2598342144",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
		assertEquals(response.getBody(), "Anfangsdatum darf nicht in der Vergangenheit liegen.");
	}

	@Test
	public void whenStartDateIsNull_PickUpTimeMustAlsoBeNull_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				0L,
				null,
				"2598342144",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
		assertEquals(response.getBody(), "Wenn bei \"Abholbar ab\" eine Uhrzeit eingegeben wird, muss auch ein Anfangsdatum eingegeben werden.");
	}

	@Test
	public void whenStartDateIsBeforeEndDate_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				0L,
				"2598342143.66",
				"2598342142",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
		assertEquals(response.getBody(), "Enddatum darf nicht vor Anfangsdatum liegen.");
	}

	@Test
	public void whenStartDateIsNullButEndDateIsProvided_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				0L,
				null,
				"2598342142",
				"12:15",
				"17:30",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
		assertEquals(response.getBody(), "Wenn bei \"Abholbar ab\" eine Uhrzeit eingegeben wird, muss auch ein Anfangsdatum eingegeben werden.");
	}

	@Test
	public void whenStartDateIsEqualToEndDateAndPickUpTimeIsAfterReturnTime_thenReturn400() {
		ResponseEntity response = productService.validateInput( null,
				"test",
				"title",
				"testString",
				null,
				0L,
				"2598342142.00",
				"2598342142",
				"12:15",
				"12:14",
				null);
		System.out.println(response);
		assertEquals(response.getStatusCodeValue(), 400);
		assertEquals(response.getBody(), "Bei gleichem Anfang- und Enddatum darf die Rückgabezeit nicht vor der frühesten Abholzeit liegen.");
	}

	@Ignore
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

// ...test not implemented yet

	}

	// fix with 11.5 from https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/test-method.html
//	@Test
//	@WithUserDetails(value="Harald", userDetailsServiceBeanName="CustomUserDetailsService")
//	public void testAuthentication() throws Exception {
//		mvc.perform(post("/authenticate"))
//				.andExpect(status().isOk());
//	}


}