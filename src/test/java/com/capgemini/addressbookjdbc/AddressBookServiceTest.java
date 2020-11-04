package com.capgemini.addressbookjdbc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AddressBookServiceTest {
	@Test
	public void contactsWhenRetrievedFromDB_ShouldMatchCount() {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactList = addressBookService.readContactData();
		Assert.assertEquals(7, contactList.size());
	}
}
