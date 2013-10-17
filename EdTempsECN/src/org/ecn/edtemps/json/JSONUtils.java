package org.ecn.edtemps.json;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

public class JSONUtils {

	/**
	 * Créé un JsonArray à partir d'une collection itérable (List par exemple) de JSONAble
	 * @param jsonAbles Collection itérable de JSONAble
	 * @return Array généré
	 */
	public static <T extends JSONAble, U extends Iterable<T>> JsonArray getJsonArray(U jsonAbles) {
		
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(T jsonAble : jsonAbles) {
			builder.add(jsonAble.toJson());
		}
		
		return builder.build();
	}
	
	public static JsonArray getJsonIntArray(Iterable<Integer> integers) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(int i : integers) {
			builder.add(i);
		}
		
		return builder.build();
	}
}
