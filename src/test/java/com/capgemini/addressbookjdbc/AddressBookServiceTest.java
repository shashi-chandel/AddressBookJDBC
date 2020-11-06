package com.capgemini.addressbookjdbc;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.gson.Gson;

import com.capgemini.addressbookjdbc.AddressBookService.IOService;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AddressBookServiceTest {

	@Test
	public void contactsWhenRetrievedFromDB_ShouldMatchCount() {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactList = addressBookService.readContactData();
		Assert.assertEquals(22, contactList.size());
	}

	@Test
	public void givenNewSalaryForEmployee_WhenUpdatedUsingPreparedStatement_ShouldSyncWithDB() {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactList = addressBookService.readContactData();
		addressBookService.updateContactDetails("Sam", "Street 5");
		boolean result = addressBookService.checkContactInSyncWithDB("Sam");
		Assert.assertTrue(result);
	}

	@Test
	public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData();
		LocalDate startDate = LocalDate.of(2019, 11, 01);
		LocalDate endDate = LocalDate.now();
		List<Contact> contactList = addressBookService.readContactDataForGivenDateRange(startDate, endDate);
		Assert.assertEquals(5, contactList.size());
	}

	@Test
	public void givenContacts_RetrieveNumberOfContacts_ByCityOrState() {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData();
		Map<String, Integer> contactByCityOrStateMap = addressBookService.readContactByCityOrState();
		Assert.assertEquals(true, contactByCityOrStateMap.get("California").equals(12));
	}

	@Test
	public void givenNewContact_WhenAdded_ShouldSyncWithDB() {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData();
		LocalDate date = LocalDate.of(2020, 02, 20);
		addressBookService.addContactToDatabase("Eric", "Hector", "Galaxy Mall", "San Diego", "California", 700012,
				"9908487454", "eric01@gmail.com", "Family");
		boolean result = addressBookService.checkContactInSyncWithDB("Eric");
		Assert.assertTrue(result);
	}

	@Test
	public void givenContacts_WhenAddedToDB_ShouldMatchEmployeeEntries() {
		Contact[] arrayOfEmployee = {
				new Contact("Ayan", "Mallik", "Street 6", "Pune", "Maharashtra", 507012, "9865430031",
						"malikayan@gmail.com", "Casual"),
				new Contact("Shreya", "Ghoshal", "Street 7", "Mumbai", "Maharashtra", 580012, "9865854331",
						"ghoshalshreya@gmail.com", "Casual") };
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readData(IOService.DB_IO);
		Instant start = Instant.now();
		addressBookService.addContact(Arrays.asList(arrayOfEmployee));
		Instant end = Instant.now();
		System.out.println("Duration without thread : " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		addressBookService.addEmployeeToPayrollWithThreads(Arrays.asList(arrayOfEmployee));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with Thread : " + Duration.between(threadStart, threadEnd));
		Assert.assertEquals(25, addressBookService.countEntries(IOService.DB_IO));
	}

	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	public Contact[] getContactList() {
		Response response = RestAssured.get("/contacts");
		System.out.println("Contact entries in JSON Server :\n" + response.asString());
		Contact[] arrayOfContacts = new Gson().fromJson(response.asString(), Contact[].class);
		return arrayOfContacts;
	}

	public Response addContactToJsonServer(Contact contactData) {
		String contactJson = new Gson().toJson(contactData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(contactJson);
		return request.post("/contacts");
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		Contact[] arrayOfContacts = getContactList();
		AddressBookService addressBookService;
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		long entries = addressBookService.countEntries(IOService.REST_IO);
		Assert.assertEquals(4, entries);
	}

	@Test
	public void givenNewContact_WhenAdded__ShouldMatch() {
		AddressBookService addressBookService;
		Contact[] arrayOfContacts = getContactList();
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		Contact contactData = null;
		contactData = new Contact(0, "Shivam", "Sarkar", "Station Road", "Bano", "Jharkhand", 835102, "9908487489",
				"shivamgolu@gmail.com", "friend");
		Response response = addContactToJsonServer(contactData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		contactData = new Gson().fromJson(response.asString(), Contact.class);
		addressBookService.addContactToJSONServer(contactData, IOService.REST_IO);
		long entries = addressBookService.countEntries(IOService.REST_IO);
		Assert.assertEquals(4, entries);
	}

}
