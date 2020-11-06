package com.capgemini.addressbookjdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressBookService {
	private List<Contact> contactList;
	private AddressBookDBService addressBookDBService;
	private Map<String, Integer> contactByCityOrState;
	
	public enum IOService {
		DB_IO,REST_IO
	}
	
	public AddressBookService(List<Contact> contactList) {
		this();
		this.contactList = new ArrayList<>(contactList);
	}

	public AddressBookService() {
		addressBookDBService = AddressBookDBService.getInstance();
	}

	public List<Contact> readContactData() {
		this.contactList = addressBookDBService.readData();
		return contactList;
	}
	
	public long countEntries(IOService ioService) {
		return contactList.size();
	}

	public List<Contact> readData(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.contactList = addressBookDBService.readData();
		return contactList;
	}
	
	public void updateContactDetails(String name, String address) {
		int result = addressBookDBService.updateEmployeeData(name, address);
		if (result == 0)
			return;
		Contact personInfo = this.getContactData(name);
		if (personInfo != null)
			personInfo.address = address;
	}

	private Contact getContactData(String name) {
		return this.contactList.stream().filter(contact -> contact.firstName.equals(name)).findFirst().orElse(null);
	}

	public boolean checkContactInSyncWithDB(String name) {
		List<Contact> contactList = addressBookDBService.getContactDataByName(name);
		return contactList.get(0).equals(getContactData(name));
	}
	
	public List<Contact> readContactDataForGivenDateRange(LocalDate startDate, LocalDate endDate) {
		this.contactList = addressBookDBService.getContactForGivenDateRange(startDate, endDate);
		return contactList;
	}
	
	public Map<String, Integer> readContactByCityOrState() {
		this.contactByCityOrState=addressBookDBService.getContactsByCityOrState();
		return contactByCityOrState;
	}
	public void addContactToDatabase(String firstName, String lastName, String address, String city, String state,
			int zip, String phone, String email, String type) {
		contactList.add(addressBookDBService.addContact(firstName, lastName, address, city, state, zip, phone, email, type));

	}
	
	public void addContactToDB(String firstName, String lastName, String address, String city, String state, int zip,
			String phone, String email, String addressBookType) {
		contactList.add(addressBookDBService.addContact(firstName, lastName, address, city, state, zip, phone, email,
				addressBookType));

	}
	
	public void addContactToJSONServer(Contact contactData, IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.addContactToDB(contactData.firstName, contactData.lastName, contactData.address, contactData.city,
					contactData.state, contactData.zip, contactData.phoneNumber, contactData.email,
					contactData.addressBookType);
		contactList.add(contactData);

	}

	public void addContact(List<Contact> contactDataList) {
		contactDataList.forEach(contactData -> {
			System.out.println("Employee being added : " + contactData.firstName);
			this.addContactToDB(contactData.firstName, contactData.lastName, contactData.address, contactData.city,
					contactData.state, contactData.zip, contactData.phoneNumber, contactData.email,
					contactData.addressBookType);
			System.out.println("Employee added : " + contactData.firstName);
		});
		System.out.println("" + this.contactList);
	}

	public void addEmployeeToPayrollWithThreads(List<Contact> contactDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
		contactDataList.forEach(contactData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(contactData.hashCode(), false);
				System.out.println("Employee being added : " + Thread.currentThread().getName());
				this.addContactToDB(contactData.firstName, contactData.lastName, contactData.address, contactData.city,
						contactData.state, contactData.zip, contactData.phoneNumber, contactData.email,
						contactData.addressBookType);
				employeeAdditionStatus.put(contactData.hashCode(), true);
				System.out.println("Employee added : " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, contactData.firstName);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		System.out.println("" + this.contactList);
	}

}
