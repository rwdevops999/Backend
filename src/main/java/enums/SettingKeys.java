package enums;

public enum SettingKeys {
	TENANT("Tenant", "OCI"),
	REGION("Region", "OCI");
	
	private final String key;
	private final String type;
	
	SettingKeys(String key, String type) {
		this.key = key;
		this.type = type;
	}
	
	public String getKey( ) {
		return this.key;
	}

	public String getType( ) {
		return this.type;
	}
}
