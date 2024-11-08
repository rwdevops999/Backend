package com.tutopedia.backend.util;

import java.util.Map;
import java.util.stream.Collectors;

public class ParamBuilder {
	public static String build(Map<String,Object> params) {
		String str = params.keySet().stream()
		  .sorted()
		  .map(k -> k+"="+params.get(k))
		  .collect(Collectors.joining("&"));
			  
		  return str;
	}
}
