package org.ecn.edtemps.json;

import javax.json.JsonValue;

/**
 * Interface que tous les objets suceptibles d'être transformés au format JSON doivent implémenter
 * 
 * @author Joffrey
 */
public interface JSONAble {
	public JsonValue toJson();
}
