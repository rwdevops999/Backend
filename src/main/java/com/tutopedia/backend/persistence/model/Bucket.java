package com.tutopedia.backend.persistence.model;

import java.util.Date;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name = "bucket", schema = "public")
public class Bucket {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bucket_seq")
	@SequenceGenerator(name = "bucket_seq", allocationSize = 1)
	@Getter
	@Setter
	private long id;
	
	@Column(name = "name", nullable = false)
	@Getter @Setter 
	@Length(max = 255, message = "error.name.length")
	private String name;
	
	@Column(name = "selected")
	@Getter @Setter
	private boolean selected = false;

	@Column(name = "favorite")
	@Getter @Setter
	private boolean favorite = false;
	
	@Column(name = "tutorials")
	@Getter @Setter
	private Integer tutorials = 0;

	@Column(name = "updatedate")
	@Getter @Setter
	private Date updateDate = new Date();

	public Bucket(String name) {
		this.name = name;
	}

	public Bucket(Long id, String name, Boolean selected) {
		this(name);
		this.id = id;
		this.selected = selected;
	}

	@Override
	public String toString() {
		return "Bucket "
				+ "["
				+ "id=" + id 
				+ ", name=" + name 
				+ ", selected=" + selected 
				+ ", favorite=" + favorite 
				+ ", tutorials =" + tutorials
				+ ", date =" + updateDate
				+ "]";
	}
}
