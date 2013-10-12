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
		this.effectuerRequete("POST", "identification/connection", { username: identifiant, password: pass }, 
			function(success, response) {
				if(success) { // Succès de la requête (pas forcément de la connexion)
					if(response.resultCode == 0) { // Succès de l'identification
						me._token = response.data.token;
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
	
	/* Récupère le token de connexion en cours */
	RestManager.prototype.getToken = function() {
		return this._token;
	};
	
	/* Dans le cas où l'application possède un token de connexion,
	 * vérifie auprès du serveur que ce token est valide.
	 * Param callback : fonction de rappel appelée une fois la requête effectuée. Arguments : 
	 * - networkSuccess (booléen) : false si il y a eu une erreur de réseau
	 * - validConnection (booléen) : true si il existe un token de connexion valide
	 */
	RestManager.prototype.checkConnection = function(callback) {
		if(this._token) {
			this.effectuerRequete("GET", "identification/checkconnection", { token: this._token }, function(success, data) {
				if(success) {
					if(data.resultCode == 0) { // Succès
						callback(true, true);
					}
					else {
						callback(true, false);
					}
				}
				else {
					callback(false);
				}
			});
		}
		else {
			callback(true, false);
		}
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