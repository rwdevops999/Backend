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
@Table(name = "setting", schema = "public")
public class Setting {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "setting_seq")
	@SequenceGenerator(name = "setting_seq", allocationSize = 1)
	@Getter
	@Setter
	private long id;
	
	@Column(name = "tkey", nullable = false)
	@Getter @Setter 
	@Length(max = 50)
	private String key;
	
	@Column(name = "tvalue")
	@Getter @Setter
	@Length(max = 255)
	private String value;

	@Column(name = "type", nullable = false)
	@Getter @Setter 
	@Length(max = 10)
	private String type;
	
	@Override
	public String toString() {
		return "Setting "
				+ "["
				+ "id=" + id 
				+ ", type=" + type
				+ ", key=" + key
				+ ", value=" + value
				+ "]";
	}

	public Setting(@Length(max = 50) String key, @Length(max = 255) String value, @Length(max = 10) String type) {
		this.id = 0;
		this.key = key;
		this.value = value;
		this.type = type;
	}

	public Setting(@Length(max = 50) String key) {
		this(key, null, null);
	}
}
