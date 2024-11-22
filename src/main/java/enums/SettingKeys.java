package enums;

import java.util.Arrays;
import java.util.Optional;

public enum SettingKeys {
	TENANT("Tenant", "OCI"),
	REGION("Region", "OCI");
	
	private final String key;
	private final String type;
	
	SettingKeys(String key, String type) {
		this.key = key;
		this.type = type;
	}
	
	public static String getTypeOf(String key) {
		Optional<SettingKeys> result = Arrays.asList(SettingKeys.values()).stream().filter((SettingKeys sk) -> sk.getKey() == key).findFirst();
		
		if (result.isEmpty()) {
			System.out.println("SettingKeys not found for " + key);
			return null;
		}
		
		return result.get().getType();
	}
	
	public String getKey( ) {
		return this.key;
	}

	public String getType( ) {
		return this.type;
	}
}
