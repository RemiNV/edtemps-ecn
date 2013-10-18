/* Module effectuant les requêtes sur le serveur,
 * et gérant la connexion à celui-ci. Gère entre autres
 * le token de connexion */
define(["jquery"], function() {
	
	// Constructeur
	var RestManager = function() {
		
		// Récupération du dernier token de connection depuis le stockage local.
		// Il peut avoir expiré.
		if(window.localStorage) {
			this._token = window.localStorage["token"];
		}
		else
			this._token = null; // Navigateurs ne supportant pas localStorage
			
		this._connected = false; // Appeler connexion() ou checkConnexion() pour mettre à jour ce statut
	};
	
	// Correspondent aux codes définis dans le Java
	RestManager.resultCode_NetworkError = -1;
	RestManager.resultCode_Success = 0;
	RestManager.resultCode_IdentificationError = 1;
	RestManager.resultCode_LdapError = 3;
	
	RestManager.prototype.setToken = function(token) {
		this._token = token;
		if(window.localStorage) {
			window.localStorage["token"] = token;
		}
	};
	
	/* Fonction de connexion auprès du serveur.
	 * Param identifiant : identifiant de l'utilisateur
	 * Param pass : mot de passe de l'utilisateur
	 * Param callback : fonction de rappel appelée pour fournir les résultats de la requête
	 * 	La fonction callback prend les arguments : 
	 *  - resultCode (entier) : résultat de la connexion, correspondant à RestManager.resultCode_*
	 * Valeur de retour : aucune */
	RestManager.prototype.connexion = function(identifiant, pass, callback) {
		var me = this;
		
		// Connection depuis le serveur
		this.effectuerRequete("POST", "identification/connection", { username: identifiant, password: pass }, 
			function(response) {
			
				if(response.resultCode == RestManager.resultCode_Success) { // Succès de l'identification
					me.setToken(response.data.token);
					me._isConnected = true;
				}
				
				callback(response.resultCode);
			}
		);
	};
	
	/**
	 * Fonction de déconnexion auprès du serveur
	 * Param callback : fonction appelée une fois la requête terminée. Arguments : 
	 * - resultCode (entier) : code de résultat renvoyé par le serveur. RestManager.resultCode_Success en cas de succès de la déconnexion. */
	RestManager.prototype.deconnexion = function(callback) {
		this.effectuerRequete("GET", "identification/disconnect", { token: this._token }, function(data) {
			// On considère que la déconnexion est un succès si il y a une erreur d'identification
			if(data.resultCode === RestManager.resultCode_Success || data.resultCode === RestManager.resultCode_IdentificationError) {
				this._isConnected = false;
				this._token = null;
			}
			
			callback(data.resultCode);
		});
	};
	
	/* Récupère le token de connexion en cours */
	RestManager.prototype.getToken = function() {
		return this._token;
	};
	
	/* Dans le cas où l'application possède un token de connexion,
	 * vérifie auprès du serveur que ce token est (encore) valide.
	 * Param callback : fonction de rappel appelée une fois la requête effectuée. Arguments : 
	 * - resultCode (int) : code de retour, RestManager.resultCode_Success en cas de succès
	 */
	RestManager.prototype.checkConnection = function(callback) {
		var me = this;
		if(this._token) {
			this.effectuerRequete("GET", "identification/checkconnection", { token: this._token }, function(data) {
				if(data.resultCode == RestManager.resultCode_Success) {
					me._isConnected = true;
				}
			
				callback(data.resultCode);
			});
		}
		else {
			callback(RestManager.resultCode_IdentificationError); // Aucune requête serveur nécessaire
		}
	};
	
	/* Indique si l'utilisateur en cours est connecté.
	 * L'utilisateur peut être connecté si on a fait appel à connexion(), ou à checkConnection si un token valide était enregistré
	 * (si les informations de connexion ont été stockées dans le navigateur par exemple) */
	RestManager.prototype.isConnected = function() {
		return this._isConnected;
	};
	
	/* Effectue une requête AJAX avec la méthode donnée ('GET', 'POST', 'PUT' ou 'DELETE'),
	 * pour l'URL donnée, avec les données fournies.
	 * La fonction de callback prend en argument : 
	 * - data (object) : résultat de la requête.
	 * En cas d'échec de la connexion, il ne contient qu'un attribut resultCode correspondant à
	 * RestManager.resultCode_NetworkError.
	 */
	RestManager.prototype.effectuerRequete = function(method, url, data, callback) {
		
		$.ajax(url, {
			data: data,
			type: method,
			timeout: 15000 // 15 sec.
		})
		.done(function(data) {
			callback(data);
		})
		.fail(function(data) {
			callback({ resultCode: RestManager.resultCode_NetworkError });
		});
	};
	
	// Renvoyer RestManager dans cette fonction le définit comme l'objet de ce fichier de module
	return RestManager;
});