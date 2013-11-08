package org.ecn.edtemps.json;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonValue;

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
	
	public static JsonArray getJsonStringArray(Iterable<String> strings) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(String s : strings) {
			builder.add(s);
		}
		
		return builder.build();
	}
	
	/**
	 * Récupère une liste d'entiers à partir d'un JsonArray.
	 * La conversion peut ne pas être exacte, par exemple si le nombre dépasse la capacité d'un int ou est à virgule
	 * 
	 * @param array tableau json à parcourir
	 * @return Une liste d'entiers. Aucun des entiers n'est null.
	 * @throws ClassCastException Si une des valeurs du tableau n'est pas un nombre
	 */
	public static ArrayList<Integer> getIntegerArrayList(JsonArray array) throws ClassCastException {
		ArrayList<Integer> res = new ArrayList<Integer>(array.size());
		
		for(JsonValue v : array) {
			res.add(((JsonNumber)v).intValue());
		}
		
		return res;
	}
}
