/* Module effectuant les requ�tes sur le serveur,
 * et g�rant la connexion � celui-ci. G�re entre autres
 * le token de connexion */
define([], function() {
	
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
	 *  - identifiantsValides (bool�en) : succ�s de l'identification de l'utilisateur
	 * Valeur de retour : aucune */
	RestManager.prototype.connexion = function(identifiant, pass, callback) {
		var me = this;
		// Non encore impl�ment� : succ�s automatique
		setTimeout(function() {
			me._token = "tokendetest";
			me._isConnected = true;
			callback(true, true); // Succ�s
		}, 1000); // Appel de la fonction de callback apr�s 1 seconde
	};

	/* Indique si l'utilisateur en cours est connect�.
	 * L'utilisateur peut �tre connect� sans appel de connexion() pr�alable
	 * (si les informations de connexion ont �t� stock�es dans le navigateur par exemple) */
	RestManager.prototype.isConnected = function() {
		return this._isConnected;
	}
	
	// Renvoyer RestManager dans cette fonction le d�finit comme l'objet de ce fichier de module
	return RestManager;
});