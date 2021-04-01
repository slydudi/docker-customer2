package sd.springboot;

import java.util.UUID;


public class Customer {

    private UUID id;

    private String pesel;

    private String firstName;

    private String lastName;
    


    public Customer(UUID id, String pesel, String firstName, String lastName){
        this.id 		= id;
        this.pesel 		= pesel;
        this.firstName 	= firstName;
        this.lastName 	= lastName;
    }


	public UUID getId() {
		return id;
	}


	public void setId(UUID id) {
		this.id = id;
	}


	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getPesel() {
		return pesel;
	}


	public void setPesel(String pesel) {
		this.pesel = pesel;
	}

}
