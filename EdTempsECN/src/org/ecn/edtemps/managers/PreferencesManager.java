package org.ecn.edtemps.managers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classe de gestion des paramètres de l'application (adresse des serveurs distants, etc.)
 * @author Remi
 *
 */
public class PreferencesManager {
	
	private static Logger logger = LogManager.getLogger(PreferencesManager.class.getName());
	
	protected static final String CONFIG_FILENAME = "edtemps_config.xml"; 
	protected static final Properties properties = new Properties();
	protected static boolean propertiesLoaded = false;
	
	private PreferencesManager() {};
	
	/**
	 * Ensemble des entrées de préférences gérées par l'application
	 *
	 */
	public enum EdtempsPreference {
		/** URL du serveur PostgreSQL pour JDBC, clé pgsql_jdbc_url du fichier de configuration */
		PGSQL_JDBC_URL("pgsql_jdbc_url"),
		
		/** Nom d'utilisateur du serveur PostgreSQL, clé pgsql_user du fichier de configuration */
		PGSQL_USER("pgsql_user"),
		
		/** Mot de passe du serveur PostgreSQL, clé pgsql_pass du fichier de configuration */
		PGSQL_PASS("pgsql_pass"),
		
		/** Adresse du serveur LDAP, clé ldap_host du fichier de configuration */
		LDAP_HOST("ldap_host"),
		
		/** Port du serveur LDAP, clé ldap_port du fichier de configuration */
		LDAP_PORT("ldap_port"),
		
		/** Indique si la connexion au serveur LDAP doit utiliser SSL, clé ldap_use_ssl du fichier de configuration */
		LDAP_USE_SSL("ldap_use_ssl");
		
		private String key;

		EdtempsPreference(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return key;
		}
	}
	
	/**
	 * Chargement des préférences depuis le fichier {@link PreferencesManager#CONFIG_FILENAME}
	 * N'effectue le chargement qu'une fois par chargement de la classe
	 */
	protected static synchronized void loadPreferences() {
		if(propertiesLoaded) {
			return;
		}
		
		try {
			
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILENAME);
			properties.loadFromXML(is);
			propertiesLoaded = true;
		} catch (IOException e) {
			String message = "Erreur de chargement du fichier de configuration " + CONFIG_FILENAME + " ; l'application ne peut pas s'exécuter correctement";
			logger.fatal(message, e);
			throw new RuntimeException(message);
		}
		
	}
	
	/**
	 * Récupération d'une valeur de paramétrage de l'application
	 * @param preference Clé de la préférence stockée
	 * @return Valeur de la préférence
	 */
	public static String getPreference(EdtempsPreference preference) {
		loadPreferences();
		String value = properties.getProperty(preference.getKey());
		
		if(value == null) {
			logger.error("Valeur de configuration non trouvée : " + preference.getKey());
			return "undefined";
		}
		
		return value;
	}
}
