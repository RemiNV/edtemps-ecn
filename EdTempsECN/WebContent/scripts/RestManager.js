/* Module effectuant les requêtes sur le serveur,
 * et gérant la connexion à celui-ci. Gère entre autres
 * le token de connexion */
define([], function() {
	
	// Constructeur
	var RestManager = function() {
		this._token = null; // Token non défini au départ
		this._isConnected = false;
	};
	
	/* Fonction de connexion auprès du serveur.
	 * Param identifiant : identifiant de l'utilisateur
	 * Param pass : mot de passe de l'utilisateur
	 * Param callback : fonction de rappel appelée pour fournir les résultats de la requête
	 * 	La fonction callback prend les arguments : 
	 * 	- success (booléen) : succès de la connexion au serveur
	 *  - identifiantsValides (booléen) : succès de l'identification de l'utilisateur
	 * Valeur de retour : aucune */
	RestManager.prototype.connexion = function(identifiant, pass, callback) {
		var me = this;
		// Non encore implémenté : succès automatique
		setTimeout(function() {
			me._token = "tokendetest";
			callback(true, true); // Succès
		}, 1000); // Appel de la fonction de callback après 1 seconde
	};

	/* Indique si l'utilisateur en cours est connecté.
	 * L'utilisateur peut être connecté sans appel de connexion() préalable
	 * (si les informations de connexion ont été stockées dans le navigateur par exemple) */
	RestManager.prototype.isConnected = function() {
		return this._isConnected;
	}
	
	// Renvoyer RestManager dans cette fonction le définit comme l'objet de ce fichier de module
	return RestManager;
});