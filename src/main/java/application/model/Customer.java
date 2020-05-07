package application.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "All details about the Employee. ")
public class Customer {
	@ApiModelProperty(notes = "The database generated employee ID")
	private String _id;
    
    @SuppressWarnings("unused")
	private String _rev;
    
    private String username;
    private String password;

	@ApiModelProperty(notes = "The employee first name")
	private String firstName;

	@ApiModelProperty(notes = "The employee last name")
	private String lastName;

	@ApiModelProperty(notes = "The employee email id")
	private String email;

	@ApiModelProperty(notes = "The employee image url")
	private String imageUrl;
	
	public String getCustomerId() {
		return _id;
	}
	public void setCustomerId(String customerId) {
		this._id = customerId;
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
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

}
