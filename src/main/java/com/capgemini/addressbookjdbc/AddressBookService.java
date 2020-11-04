package com.capgemini.addressbookjdbc;

import java.util.List;

public class AddressBookService {
	private List<Contact> contactList;
	private AddressBookDBService addressBookDBService;

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

}
