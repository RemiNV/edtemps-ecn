package org.ecn.edtemps.json;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * Classe d'utilitaires pour la génération d'objets JSON
 * 
 * @author Remi
 */
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
	
	
	/**
	 * Créé un JsonArray à partir d'une collection itérable (List par exemple) d'entiers
	 * 
	 * @param integers itérable d'entiers
	 * @return Array généré
	 */
	public static JsonArray getJsonIntArray(Iterable<Integer> integers) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		for(int i : integers) {
			builder.add(i);
		}
		
		return builder.build();
	}
	
	
	/**
	 * Créé un JsonArray à partir d'une collection itérable (List par exemple) de string
	 * 
	 * @param strings itérable de string
	 * @return Array généré
	 */
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
	
	/**
	 * Récupère une liste d'entiers à partir d'un JsonArray, et supprime les doublons.
	 * La conversion peut ne pas être exacte, par exemple si le nombre dépasse la capacité d'un int ou est à virgule.
	 * 
	 * @param array tableau json à parcourir
	 * @return Une liste d'entiers. Aucun des entiers n'est null.
	 * @throws ClassCastException Si une des valeurs du tableau n'est pas un nombre
	 */
	public static ArrayList<Integer> getIntegerArrayListSansDoublons(JsonArray array) throws ClassCastException {
		ArrayList<Integer> resDoublons = getIntegerArrayList(array);
		
		HashSet<Integer> set = new HashSet<Integer>(resDoublons.size());
		set.addAll(resDoublons);
		
		return new ArrayList<Integer>(set);
	}
	
	protected static JsonArray getJsonArrayOrNull(JsonObject object, String key) {
		return object.containsKey(key) && !object.isNull(key) ? object.getJsonArray(key) : null;
	}
	
	protected static JsonNumber getJsonNumberOrNull(JsonObject object, String key) {
		return object.containsKey(key) && !object.isNull(key) ? object.getJsonNumber(key) : null;
	}
	
	public static class EvenementClientRepetition {
		public Date dateDebut;
		public Date dateFin;
		public ArrayList<Integer> idSalles;
		public ArrayList<Integer> idEvenementsSallesALiberer;
	}
	
	public static class EvenementClient extends EvenementClientRepetition {
		public String nom;
		public ArrayList<Integer> idCalendriers;
		public ArrayList<Integer> idIntervenants;
		public ArrayList<Integer> idResponsables;
		public Integer idEvenement;
	}
	
	/**
	 * Sous-méthode permettant de lire un EvenementClientRepetition à partir d'un JsonObject lu
	 * @param jsonEvenement
	 * @return EvenementClientRepetition
	 * @throws JsonException
	 * @throws ClassCastException
	 */
	public static EvenementClientRepetition readEvenementClientRepetition(JsonObject jsonEvenement) throws JsonException, ClassCastException {
		JsonNumber jsonDebut = getJsonNumberOrNull(jsonEvenement, "dateDebut"); 
		JsonNumber jsonFin = getJsonNumberOrNull(jsonEvenement, "dateFin");
		JsonArray jsonIdSalles = getJsonArrayOrNull(jsonEvenement, "salles");
		JsonArray jsonIdEvenementsSallesALiberer = getJsonArrayOrNull(jsonEvenement, "evenementsSallesALiberer");
		
		EvenementClientRepetition res = new EvenementClientRepetition();
		
		res.dateDebut = jsonDebut == null ? null : new Date(jsonDebut.longValue());
		res.dateFin = jsonFin == null ? null : new Date(jsonFin.longValue());
		res.idSalles = jsonIdSalles == null ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdSalles);
		res.idEvenementsSallesALiberer = jsonIdEvenementsSallesALiberer == null ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdEvenementsSallesALiberer);
		
		return res;
	}
	
	/**
	 * Sous-méthode permettant de lire un EvenementClient à partir d'un JsonObject lu
	 * @param jsonEvenement
	 * @return EvenementClientRepetition
	 */
	public static EvenementClient readEvenementClient(JsonObject jsonEvenement) throws JsonException, ClassCastException {
		
		EvenementClientRepetition evenementRepetition = readEvenementClientRepetition(jsonEvenement);
		
		JsonArray jsonIdCalendriers = getJsonArrayOrNull(jsonEvenement, "calendriers");
		JsonArray jsonIdIntervenants = getJsonArrayOrNull(jsonEvenement, "intervenants");
		JsonArray jsonIdResponsables = getJsonArrayOrNull(jsonEvenement, "responsables");
		JsonNumber jsonIdEvenement = getJsonNumberOrNull(jsonEvenement, "id");
		
		EvenementClient res = new EvenementClient();
		
		res.nom = jsonEvenement.containsKey("nom") && !jsonEvenement.isNull("nom") ? jsonEvenement.getString("nom") : null;
		res.dateDebut = evenementRepetition.dateDebut;
		res.dateFin = evenementRepetition.dateFin;
		res.idSalles = evenementRepetition.idSalles;
		res.idEvenementsSallesALiberer = evenementRepetition.idEvenementsSallesALiberer;
		res.idCalendriers = jsonIdCalendriers == null ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdCalendriers);
		
		res.idIntervenants = jsonIdIntervenants == null ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdIntervenants);
		res.idResponsables = jsonIdResponsables == null ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdResponsables);
		
		res.idEvenement = jsonIdEvenement == null ? null : jsonIdEvenement.intValue();
		
		return res;
	}
	
	/**
	 * Parsing d'événements envoyés par le client pour ajout et modification
	 * @param strEvenements JSON d'un tableau d'événements au format client
	 * @return Objets parsés
	 * @throws JsonException
	 * @throws ClassCastException
	 */
	public static ArrayList<EvenementClientRepetition> getEvenementsClientRepetition(String strEvenements) throws JsonException, ClassCastException {
		JsonReader reader = Json.createReader(new StringReader(strEvenements));
		JsonArray array = reader.readArray();
		int size = array.size();
		ArrayList<EvenementClientRepetition> res = new ArrayList<EvenementClientRepetition>(size);

		for(int i=0; i<size; i++) {
			res.add(readEvenementClientRepetition(array.getJsonObject(i)));
		}
		
		reader.close();
		return res;
	}
	
	/**
	 * Parsing d'un événement envoyé par le client pour ajout et modification
	 * @param strEvenement JSON d'un événement au format client
	 * @return Objets parsés
	 * @throws JsonException
	 * @throws ClassCastException
	 */
	public static EvenementClient getEvenementClient(String strEvenement) throws JsonException, ClassCastException {
		
		JsonReader reader = Json.createReader(new StringReader(strEvenement));
		EvenementClient res = readEvenementClient(reader.readObject());
		
		reader.close();
		
		return res;
	}
}
