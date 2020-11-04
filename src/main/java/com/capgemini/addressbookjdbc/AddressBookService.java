package com.capgemini.addressbookjdbc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AddressBookService {
	private List<Contact> contactList;
	private AddressBookDBService addressBookDBService;
	private Map<String, Integer> contactByCityOrState;
	
	public AddressBookService(List<Contact> contactList) {
		this();
		this.contactList = contactList;
	}

	public AddressBookService() {
		addressBookDBService = AddressBookDBService.getInstance();
	}

	public List<Contact> readContactData() {
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


}
