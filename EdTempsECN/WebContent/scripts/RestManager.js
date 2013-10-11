/* Module effectuant les requ�tes sur le serveur,
 * et g�rant la connexion � celui-ci. G�re entre autres
 * le token de connexion */
define(["jquery"], function() {
	
	// Constructeur
	var RestManager = function() {
		this._token = null; // Token non d�fini au d�part
		this._isConnected = false;
	};
	
	/* Fonction de connexion aupr�s du serveur.
	 * Param identifiant : identifiant de l'utilisateur
	 * Param pass : mot de passe de l'utilisateur
	 * Param callback : fonction de rappel appel�e pour fournir les r�sultats de la requ�te
	 * 	La fonction callback prend les arguments : 
	 * 	- success (bool�en) : succ�s de la connexion au serveur
	 *  - identifiantsValides (bool�en) : succ�s de l'identification de l'utilisateur, fourni en cas de succ�s de la connexion
	 * Valeur de retour : aucune */
	RestManager.prototype.connexion = function(identifiant, pass, callback) {
		var me = this;
		
		// Connection depuis le serveur
		this.effectuerRequete("PUT", "identification/connexion", { username: identifiant, password: pass }, 
			function(success, data) {
				if(success) { // Succ�s de la requ�te (pas forc�ment de la connexion)
					if(data.resultCode == 0) { // Succ�s de l'identification
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

	/* Indique si l'utilisateur en cours est connect�.
	 * L'utilisateur peut �tre connect� sans appel de connexion() pr�alable
	 * (si les informations de connexion ont �t� stock�es dans le navigateur par exemple) */
	RestManager.prototype.isConnected = function() {
		return this._isConnected;
	};
	
	/* Effectue une requ�te AJAX avec la m�thode donn�e ('GET', 'POST', 'PUT' ou 'DELETE'),
	 * pour l'URL donn�e, avec les donn�es fournies.
	 * La fonction de callback prend en argument : 
	 * - success (bool�en) : succ�s de la requ�te
	 * - data (object) : r�sultat de la requ�te (non fourni si pas de succ�s)
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
	
	// Renvoyer RestManager dans cette fonction le d�finit comme l'objet de ce fichier de module
	return RestManager;
});