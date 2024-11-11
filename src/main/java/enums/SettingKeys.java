package enums;

public enum SettingKeys {
	TENANT("OCI_Tenant"),
	REGION("OCI_Region");
	
	private final String key;
	
	SettingKeys(String key) {
		this.key = key;
	}
	
	public String getKey( ) {
		return this.key;
	}
}
