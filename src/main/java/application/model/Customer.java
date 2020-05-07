package application.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "All details about the Customer ")
public class Customer {
	@ApiModelProperty(notes = "The database generated customer ID")
	@Getter @Setter private String _id;

	@ApiModelProperty(notes = "Revision represents an opaque hash value over the contents of a document.")
    @SuppressWarnings("unused")
	@Getter @Setter private String _rev;

	@ApiModelProperty(notes = "The customer username")
	@Getter @Setter private String username;

	@ApiModelProperty(notes = "The customer password")
	@Getter @Setter private String password;

	@ApiModelProperty(notes = "The customer first name")
	@Getter @Setter private String firstName;

	@ApiModelProperty(notes = "The customer last name")
	@Getter @Setter private String lastName;

	@ApiModelProperty(notes = "The customer email id")
	@Getter @Setter private String email;

	@ApiModelProperty(notes = "The customer image url")
	@Getter @Setter private String imageUrl;

}
