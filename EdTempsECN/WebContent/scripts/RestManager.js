/** Module effectuant les requêtes sur le serveur, et gérant la connexion à celui-ci.
 * Gère entre autres le token de connexion.
 * @module RestManager 
 */
define(["jquery"], function() {
	/**
	 * @constructor
	 * @alias module:RestManager
	 */
	var RestManager = function() {
		
		// Récupération du dernier token de connection depuis le stockage local.
		// Il peut avoir expiré.
		if(window.localStorage) {
			this._token = window.localStorage["token"];
			this._userId = parseInt(window.localStorage["userId"]);
			
			if(isNaN(this._userId)) {
				this._userId = null;
			}
		}
		else {
			this._token = null; // Navigateurs ne supportant pas localStorage
			this._userId = null;
		}
			
		this._connected = false; // Appeler connexion() ou checkConnexion() pour mettre à jour ce statut
		
		this._identificationErrorFallback = null; // Fonction appelée en cas d'erreur d'identification
	};
	
	/**
	 * Code de retour de RestManager.
	 * Correspond aux codes définis dans le serveur Java.
	 * Accepte les valeurs : <br>
	 * - RestManager.resultCode_NetworkError<br>
	 * - RestManager.resultCode_Success<br>
	 * - RestManager.resultCode_IdentificationError<br>
	 * - RestManager.resultCode_LdapError<br>
	 * - RestManager.resultCode_NameTaken<br>
	 * - RestManager.resultCode_SalleOccupee<br>
	 * - RestManager.resultCode_AlphanumericRequired<br>
	 * - RestManager.resultCode_AuthorizationError<br>
	 * - RestManager.resultCode_MaxRowCountExceeded<br>
	 * - RestManager.resultCode_QuotaExceeded<br>
	 * - RestManager.resultCode_DayTaken
	 * @typedef ResultCode
	 * @type {number}
	 */
	RestManager.resultCode_NetworkError = -1;
	RestManager.resultCode_Success = 0;
	RestManager.resultCode_IdentificationError = 1;
	RestManager.resultCode_LdapError = 3;
	RestManager.resultCode_InvalidObject = 7;
	RestManager.resultCode_NameTaken = 8;
	RestManager.resultCode_AuthorizationError = 9;
	RestManager.resultCode_SalleOccupee = 10;
	RestManager.resultCode_AlphanumericRequired = 11;
	RestManager.resultCode_MaxRowCountExceeded = 12;
	RestManager.resultCode_QuotaExceeded = 13;
	RestManager.resultCode_DayTaken = 14;
	
	/**
	 * Enumération des actions possibles dans l'emploi du temps
	 * Correspond aux codes définis dans le serveur Java.
	 * Accepte les valeurs : <br>
	 * - RestManager.actionsEdtemps_CreerGroupe<br>
	 * - RestManager.actionsEdtemps_RattacherCalendrierGroupe<br>
	 * - RestManager.actionsEdtemps_CreerGroupeCours<br>
	 * - RestManager.actionsEdtemps_ChoisirProprietairesEvenement<br>
	 * - RestManager.actionsEdtemps_LimiteCalendriersEtendue<br>
	 * - RestManager.actionsEdtemps_GererJoursBloques
	 * @typedef actionsEdtemps
	 * @type {number}
	 */
	RestManager.actionsEdtemps_CreerGroupe = 1;
	RestManager.actionsEdtemps_RattacherCalendrierGroupe = 2;
	RestManager.actionsEdtemps_CreerGroupeCours = 3;
	RestManager.actionsEdtemps_ChoisirProprietairesEvenement = 4;
	RestManager.actionsEdtemps_LimiteCalendriersEtendue = 5;
	RestManager.actionsEdtemps_GererJoursBloques = 6;
	
	RestManager.prototype.setToken = function(token) {
		this._token = token;
		if(window.localStorage) {
			window.localStorage["token"] = token;
		}
	};

	/**
	 * Affecte une valeur à l'attribut "userId"
	 * @param {number} userId Identifiant de l'utilisateur
	 */
	RestManager.prototype.setUserId = function(userId) {
		this._userId = userId;
		if(window.localStorage) {
			window.localStorage["userId"] = userId;
		}
	};

	/**
	 * Affecte une valeur à l'attribut "listeActionsAutorisees"
	 * @param {number[]} listeActionsAutorisees Liste des identifiants des actions autorisées
	 */
	RestManager.prototype.setListeActionsAutorisees = function(listeActionsAutorisees) {
		this._listeActionsAutorisees = listeActionsAutorisees;
	};

	/** 
	 * Fonction de connexion auprès du serveur
	 * 
	 * @param {string} identifiant identifiant de l'utilisateur
	 * @param {string} pass mot de passe de l'utilisateur
	 * @param {function} callback fonction de rappel appelée pour fournir les résultats de la requête. Paramètres : resultCode
	 */
	RestManager.prototype.connexion = function(identifiant, pass, callback) {
		var me = this;
		
		// Connection depuis le serveur
		this.effectuerRequete("POST", "identification/connection", { username: identifiant, password: pass }, 
			function(response) {
			
				if(response.resultCode == RestManager.resultCode_Success) { // Succès de l'identification
					me.setToken(response.data.token);
					me.setUserId(response.data.userId);
					me.setListeActionsAutorisees(response.data.actionsAutorisees);
					me._isConnected = true;
				}
				
				callback(response.resultCode);
			}, true // On ignore le fallback en cas d'échec de connexion ici
		);
	};
	
	/**
	 * Fonction de déconnexion auprès du serveur
	 * 
	 * @param callback : fonction appelée une fois la requête terminée.
	 * Paramètre: resultCode : code de résultat renvoyé par le serveur. RestManager.resultCode_Success en cas de succès de la déconnexion.
	 */
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
	
	/**
	 * Récupération du token de l'utilisateur
	 * @return {string} le token en cours pour l'utilisateur
	 */
	RestManager.prototype.getToken = function() {
		return this._token;
	};
	
	/**
	 * Récupération de l'ID d'utilisateur de l'utilisateur actuel
	 * @return {number} ID de l'utilisateur actuel
	 */
	RestManager.prototype.getUserId = function() {
		return this._userId;
	};
	
	/**
	 * Dans le cas où l'application possède un token de connexion,
	 * vérifie auprès du serveur que ce token est (encore) valide.
	 * Paramètres de callback : 
	 * - resultCode (int) : code de retour, RestManager.resultCode_Success en cas de succès
	 * @param {function} callback Fonction de rappel appelée une fois la requête effectuée.
	 */
	RestManager.prototype.checkConnection = function(callback) {
		var me = this;
		if(this._token) {
			this.effectuerRequete("GET", "identification/checkconnection", { token: this._token }, function(data) {
				if(data.resultCode == RestManager.resultCode_Success) {
					me._isConnected = true;
					me.setUserId(data.data.id);
					me.setListeActionsAutorisees(data.data.actionsAutorisees);
				}
			
				callback(data.resultCode);
			}, true);
		}
		else {
			callback(RestManager.resultCode_IdentificationError); // Aucune requête serveur nécessaire
		}
	};
	
	/**
	 * Indique si l'utilisateur en cours est connecté.
	 * L'utilisateur peut être connecté si on a fait appel à connexion(), ou à checkConnection si un token valide était enregistré
	 * (si les informations de connexion ont été stockées dans le navigateur par exemple)
	 * @return {boolean} Vrai si l'utilisateur est connecté
	 */
	RestManager.prototype.isConnected = function() {
		return this._isConnected;
	};
	
	/**
	 * Définition d'une fonction à appeler en cas d'erreur d'identification,
	 * avant de faire échouer la requête. Permet de reconnecter l'utilisateur sans faire
	 * échouer la requête.
	 * 
	 * Paramètres de fallback :  
	 * - callback : fonction à appeler une fois la reconnexion effectuée.
	 *   La fonction callback doit être appelée avec un argument booléen indiquant si la reconnexion a réussi.
	 * 
	 * @param {function} fallback Fonction à appeler en cas d'erreur de connexion lors d'une requête.
	 */
	RestManager.prototype.setIdentificationErrorFallback = function(fallback) {
		this._identificationErrorFallback = fallback;
	};
	
	/**
	 *  Effectue une requête AJAX
	 * 
	 * <p>Lorsque le token de connexion de l'utilisateur n'est pas valide, cette méthode peut utiliser un fallback
	 * défini précédemment pour rétablir la connexion avant de renvoyer un résultat.</p>
	 * 
	 * @param {string} method Méthode à utiliser pour effectuer la requête ('GET', 'POST', 'PUT' ou 'DELETE')
	 * @param {string} url Url pour la requête
	 * @param {object} data Objet json transmis dans la requête
	 * @param {function} callback
	 * @param {boolean} ignoreFallback VRAI pour ne pas utiliser ce fallback et renvoyer un code IdentificationError
	 */
	RestManager.prototype.effectuerRequete = function(method, url, data, callback, ignoreFallback) {
		
		var me = this;
		$.ajax(url, {
			data: data,
			dataType: "json",
			type: method,
			traditional: true, //pour éviter les problèmes de retranscription des données -> ajout %5B%5D dans l'URL en méthode GET
			timeout: 15000 // 15 sec.
		}).done(function(ajaxData) {
			
			
			if(!ignoreFallback && me._identificationErrorFallback != null && ajaxData.resultCode == RestManager.resultCode_IdentificationError) {
				me._identificationErrorFallback(function(reconnectionSuccess) {
					if(reconnectionSuccess) { // Réessai
						var newData = $.extend({}, data, { token: me.getToken() });
						me.effectuerRequete(method, url, newData, callback);
					}
					else {
						callback(ajaxData);
					}
				});
			}
			else {
				callback(ajaxData);
			}
		}).fail(function() {
			callback({ resultCode: RestManager.resultCode_NetworkError });
		});
	};
	

	/**
	 * Vérifie si un utilisateur a le droit d'effectuer une action
	 * Cette méthode ne fait pas de requête au serveur. Elle va chercher l'information dans le RestManager
	 * qui a récupéré la liste des actions que l'utilisateur peut faire, au moment de sa connexion
	 * 
	 * @param {string} codeAction Action à vérifier
	 * @return {boolean} VRAI ou FAUX en fonction des droits de l'utilisateur
	 */
	RestManager.prototype.aDroit = function(codeAction) {
		if (jQuery.inArray(codeAction, this._listeActionsAutorisees)>0) {
			return true;
		} else {
			return false;
		}
	};
	
	// Renvoyer RestManager dans cette fonction le définit comme l'objet de ce fichier de module
	return RestManager;
});