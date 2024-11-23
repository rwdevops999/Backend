package com.tutopedia.backend.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file", schema = "public")
public class TutorialFile {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq")
	@SequenceGenerator(name = "file_seq", allocationSize = 1)
	@Getter
	@Column(name = "id", updatable = false, nullable = false)
	private long id;
	
	@Getter
	@Setter
	@Column(name = "tid")
	private long tutorialId;
	
	@Getter
	@Setter
	@Column(name = "type")
	private String type;
	
	@Getter
	@Setter
	@Column(name = "tfile")
	private byte[] fileContent;

	@Getter
	@Setter
	@Column(name = "repositoryid")
	private Long repositoryid;
	
	public TutorialFile() {
	}
	
	public TutorialFile(Long tid, String type, byte[] data) {
		this.tutorialId = tid;
	    this.type = type;
	    this.fileContent = data;
	  }
}
