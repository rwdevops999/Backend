package com.tutopedia.backend.persistence.model;

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
@Table(name = "tutorial", schema = "public")
public class Tutorial {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tutorial_seq")
	@SequenceGenerator(name = "tutorial_seq", allocationSize = 1)
	@Getter
	@Setter
	private long id;
	
	@Column(name = "title", nullable = false)
	@Getter @Setter 
	@Length(max = 255, message = "error.title.length")
	private String title;
	
	@Column(name = "description", nullable = false)
	@Getter @Setter
	@Length(max = 255, message = "error.description.length")
	private String description;
	
	@Column(name = "published")
	@Getter @Setter
	private boolean published = false;
		
	@Getter @Setter
	private String filename;

	public Tutorial(String title, String description, boolean published, String filename) {
		this.title = title;
		this.description = description;
		this.published = published;
		this.filename = filename;
	}

	public Tutorial(Long id, String title, String description, boolean published, String filename) {
		this(title, description, published, filename);
		this.id = id;
	}

	@Override
	public String toString() {
		return "Tutorial [id=" + id + ", title=" + title + ", desc=" + description + ", published=" + published + ", filename=" + filename + "]";
	}
}
