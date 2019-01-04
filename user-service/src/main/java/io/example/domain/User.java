package io.example.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * The {@link User} domain entity representing the identity of a user.
 *
 * @author Kenny Bastani
 */
@Table(value = "users")
public class User {

	@Id
	private Long id;

	@Column(value = "first_name")
	private String firstName;

	@Column(value = "last_name")
	private String lastName;

	@Column(value = "created_at")
	private Timestamp createdAt;

	@Column(value = "updated_at")
	private Timestamp updatedAt;

	public User() {
	}


	public User(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public User(Long id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getLastModified() {
		return updatedAt;
	}

	public void setLastModified(Timestamp lastModified) {
		this.updatedAt = lastModified;
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

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(id, user.id) &&
				Objects.equals(firstName, user.firstName) &&
				Objects.equals(lastName, user.lastName) &&
				Objects.equals(createdAt, user.createdAt) &&
				Objects.equals(updatedAt, user.updatedAt);
	}

	@Override
	public int hashCode() {

		return Objects.hash(id, firstName, lastName, createdAt, updatedAt);
	}
}
