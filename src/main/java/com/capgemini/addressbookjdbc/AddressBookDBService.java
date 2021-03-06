package com.capgemini.addressbookjdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressBookDBService {

	private static AddressBookDBService addressBookDBService;
	private PreparedStatement ContactDataStatement;

	private AddressBookDBService() {
	}

	public static AddressBookDBService getInstance() {
		if (addressBookDBService == null)
			addressBookDBService = new AddressBookDBService();
		return addressBookDBService;
	}

	public List<Contact> readData() {
		String sql = "SELECT * from address_book;";
		return this.getContactDetailsUsingSqlQuery(sql);
	}

	private List<Contact> getContactDetailsUsingSqlQuery(String sql) {
		List<Contact> ContactList = null;
		try (Connection connection = addressBookDBService.getConnection();) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			ContactList = this.getAddressBookData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ContactList;
	}

	private List<Contact> getAddressBookData(ResultSet resultSet) {
		List<Contact> contactList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				String firstName = resultSet.getString("first_Name");
				String lastName = resultSet.getString("last_Name");
				String address = resultSet.getString("address");
				String city = resultSet.getString("city");
				String state = resultSet.getString("state");
				int zip = resultSet.getInt("zip");
				String phoneNumber = resultSet.getString("phone");
				String email = resultSet.getString("email");
				String addressBookType = resultSet.getString("type");
				contactList.add(new Contact(firstName, lastName, address, city, state, zip, phoneNumber, email,
						addressBookType));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}
	
	public int updateEmployeeData(String name, String address) {
		return this.updateContactDataUsingPreparedStatement(name, address);
	}

	private int updateContactDataUsingPreparedStatement(String firstName, String address) {
		try (Connection connection = addressBookDBService.getConnection();) {
			String sql = "update contact set address=? where first_name=?;";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, address);
			preparedStatement.setString(2, firstName);
			int status = preparedStatement.executeUpdate();
			return status;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<Contact> getContactDataByName(String name) {
		List<Contact> contactList = null;
		if (this.ContactDataStatement == null)
			this.prepareStatementForContactData();
		try {
			ContactDataStatement.setString(1, name);
			ResultSet resultSet = ContactDataStatement.executeQuery();
			contactList = this.getAddressBookData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}
	
	public List<Contact> getContactForGivenDateRange(LocalDate startDate, LocalDate endDate) {
		String sql = String.format(
				"SELECT a.first_name,a.last_name,a.address,a.city,a.state,a.zip,a.phone,a.email,a.type from address_book a left join user_details u on a.last_name=u.last_name WHERE u.date_added BETWEEN '%s' AND '%s'; ",
				Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getContactDetailsUsingSqlQuery(sql);
	}

	public Map<String, Integer> getContactsByCityOrState() {
		Map<String, Integer> contactByCityOrStateMap = new HashMap<>();
		ResultSet resultSet;
		String sqlCity = "SELECT city, count(first_name) as count from address_book group by city; ";
		String sqlState = "SELECT state, count(first_name) as count from address_book group by state; ";
		try (Connection connection = addressBookDBService.getConnection()) {
			Statement statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlCity);
			while (resultSet.next()) {
				String city = resultSet.getString("city");
				Integer count = resultSet.getInt("count");
				contactByCityOrStateMap.put(city,count);
			}
			resultSet = statement.executeQuery(sqlState);
			while (resultSet.next()) {
				String state = resultSet.getString("state");
				Integer count = resultSet.getInt("count");
				contactByCityOrStateMap.put(state,count);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactByCityOrStateMap;
	}

	public Contact addContact(String firstName, String lastName, String address, String city, String state, int zip,
			String phone, String email, String type) {
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			Statement statement = connection.createStatement();
			String sql = String.format(
					"insert into address_book(first_name,last_name,address,city,state,zip,phone,email,type) values ('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
					firstName, lastName, address, city, state, zip, phone, email, type);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e.printStackTrace();
			}
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return new Contact(firstName, lastName, address, city, state, zip, phone, email, type);
	}
	
	private void prepareStatementForContactData() {
		try {
			Connection connection = addressBookDBService.getConnection();
			String sql = "SELECT * from address_book WHERE first_name=?; ";
			ContactDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/address_book_service?useSSL=false";
		String userName = "root";
		String password = "Shashi@123";
		Connection connection;
		System.out.println("Connecting to database: " + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("Connection successful: " + connection);
		return connection;
	}
}
