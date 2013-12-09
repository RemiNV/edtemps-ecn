/**
 * Module de gestion/récupération des groupes de participants
 * @module GroupeGestion
 */
define(["RestManager"], function(RestManager) {

	/**
	 * @constructor
	 * @alias module:GroupeGestion
	 */
	var GroupeGestion = function(restManager) {
		this.restManager = restManager;
	};
	
	/**
	 * Récupérer les groupes auxquels est abonné / n'est pas abonné l'utilisateur
	 * @param callback
	 */
	GroupeGestion.prototype.queryAbonnementsEtNonAbonnements = function(callback) {
		this.restManager.effectuerRequete("GET", "abonnementsetnonabonnements", {
			token: this.restManager.getToken()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data);
			}
			else {
				callback(data.resultCode);
			}
		});
	};
	
	/**
	 * Désabonner l'utilisateur du groupe dont l'id est en paramètre
	 * @param idgroupe
	 * @param callback
	 */
	GroupeGestion.prototype.seDesabonner = function(idgroupe, callback) {
		this.restManager.effectuerRequete("POST", "sedesabonner", {
			token : this.restManager.getToken(),
			idGroupe : idgroupe
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	/**
	 * Abonner l'utilisateur au groupe dont l'id est en paramètre
	 * @param idgroupe
	 * @param callback
	 */
	GroupeGestion.prototype.sAbonner = function(idgroupe, callback) {
		this.restManager.effectuerRequete("POST", "sabonner", {
			token: this.restManager.getToken(),
			idGroupe: idgroupe
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	/**
	 * Récupérer les groupes desquels l'utilisateur est propriétaire
	 * @param callback
	 */
	GroupeGestion.prototype.queryGroupesUtilisateurProprietaire = function(callback) {
		this.restManager.effectuerRequete("POST", "groupesutilisateurproprietaire", {
			token: this.restManager.getToken()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data);
			} else {
				callback(data.resultCode);
			}
		});
	};


	/**
	 * Supprimer un groupe de participants
	 * @param groupeId
	 * @param callback
	 */
	GroupeGestion.prototype.querySupprimerGroupes = function(groupeId, callback) {
		this.restManager.effectuerRequete("POST", "groupeparticipants/supprimer", {
			token: this.restManager.getToken(), id: groupeId
		}, function(data) {
			callback(data.resultCode);
		});
	};
	

	/**
	 * Ne plus être propriétaire d'un groupe de participants
	 * @param groupeId
	 * @param callback
	 */
	GroupeGestion.prototype.queryNePlusEtreProprietaire = function(groupeId, callback) {
		this.restManager.effectuerRequete("POST", "groupeparticipants/nePlusEtreProprietaire", {
			token: this.restManager.getToken(), groupeId: groupeId
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	
	/**
	 * Récupérer un groupe de participants avec toutes les données complètes
	 * @param groupeId
	 * @param callback
	 */
	GroupeGestion.prototype.queryGetGroupeComplet = function(groupeId, callback) {
		this.restManager.effectuerRequete("GET", "groupeparticipants/get", {
			token: this.restManager.getToken(), id: groupeId
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data);
			} else {
				callback(data.resultCode);
			}
		});
	};

	
	/**
	 * Récupérer les groupes et les calendriers en attente de rattachement pour l'utilisateur en cours
	 * @param callback
	 */
	GroupeGestion.prototype.queryGroupesEtCalendriersEnAttenteRattachement = function(callback) {
		this.restManager.effectuerRequete("POST", "rattachementgroupe/listermesdemandes", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data.listeGroupes, data.data.listeCalendriers);
			} else {
				callback(data.resultCode);
			}
		});
	};


	/**
	 * Décider du sort du rattachement d'un groupe à un autre (accepté ou refusé)
	 * @param etat VRAI si le rattachement est accepté, FAUX sinon
	 * @param groupeId identifiant du groupe pour lequel le rattachement a été décidé
	 * @param callback
	 */
	GroupeGestion.prototype.queryDeciderRattachementGroupe = function(etat, groupeId, callback) {
		this.restManager.effectuerRequete("POST", "rattachementgroupe/decidergroupe", {
			token: this.restManager.getToken(), id: groupeId, etat: etat
		}, function(data) {
			callback(data.resultCode);
		});
	};
	

	/**
	 * Décider du sort du rattachement d'un calendrier à un groupe (accepté ou refusé)
	 * @param etat VRAI si le rattachement est accepté, FAUX sinon
	 * @param calendrierId identifiant du calendrier demandeur
	 * @param groupeId identifiant du groupe concerné
	 * @param callback
	 */
	GroupeGestion.prototype.queryDeciderRattachementCalendrier = function(etat, calendrierId, groupeId, callback) {
		this.restManager.effectuerRequete("POST", "rattachementgroupe/decidercalendrier", {
			token: this.restManager.getToken(), calendrierId: calendrierId, groupeIdParent: groupeId, etat: etat
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	
	return GroupeGestion;
});