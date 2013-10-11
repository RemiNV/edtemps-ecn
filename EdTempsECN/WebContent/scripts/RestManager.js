/* Module effectuant les requêtes sur le serveur,
 * et gérant la connexion à celui-ci. Gère entre autres
 * le token de connexion */
define(["jquery"], function() {
	
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
	 *  - identifiantsValides (booléen) : succès de l'identification de l'utilisateur, fourni en cas de succès de la connexion
	 * Valeur de retour : aucune */
	RestManager.prototype.connexion = function(identifiant, pass, callback) {
		var me = this;
		
		// Connection depuis le serveur
		this.effectuerRequete("PUT", "identification/connexion", { username: identifiant, password: pass }, 
			function(success, data) {
				if(success) { // Succès de la requête (pas forcément de la connexion)
					if(data.resultCode == 0) { // Succès de l'identification
						me._token = data.token;
						me._isConnected = true;
						callback(true, true);
					}
					else { // Erreur d'identification
						callback(true, false);
					}
				}
				else { // Erreur de connexion au serveur
					callback(false);
				}
			}
		);
	};

	/* Indique si l'utilisateur en cours est connecté.
	 * L'utilisateur peut être connecté sans appel de connexion() préalable
	 * (si les informations de connexion ont été stockées dans le navigateur par exemple) */
	RestManager.prototype.isConnected = function() {
		return this._isConnected;
	};
	
	/* Effectue une requête AJAX avec la méthode donnée ('GET', 'POST', 'PUT' ou 'DELETE'),
	 * pour l'URL donnée, avec les données fournies.
	 * La fonction de callback prend en argument : 
	 * - success (booléen) : succès de la requête
	 * - data (object) : résultat de la requête (non fourni si pas de succès)
	 */
	RestManager.prototype.effectuerRequete = function(method, url, data, callback) {
		$.ajax(url, {
			data: data,
			type: method
		})
		.done(function(data) {
			callback(true, data);
		})
		.fail(function(data) {
			callback(false);
		});
	};
	
	// Renvoyer RestManager dans cette fonction le définit comme l'objet de ce fichier de module
	return RestManager;
});