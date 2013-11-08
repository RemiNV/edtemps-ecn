/**
 * @module CalendrierGestion
 */
define([], function() {

	/**
	 * @constructor
	 * @alias module:CalendrierGestion
	 */
	var CalendrierGestion = function(restManager) {
		this.restManager = restManager;
	};
	
	/**
	 * @typedef {Object} Calendrier
	 * @property {number} id - ID du calendrier
	 * @property {string} nom - Nom du calendrier
	 * @property {string} type - Type du calendrier (TD, TP...)
	 * @property {string} matiere - Matière du calendrier
	 * @property {number[]} proprietaires - IDs des propriétaires du calendrier 
	 */
	
	/**
	 * @callback listerCalendriersCallback
	 * @param {ResultCode} resultCode Code de retour de la requête
	 * @param {Calendrier[]} calendriers Calendriers renvoyés par la requête, en cas de succès
	 */
	
	/**
	 * Listing des calendriers appartenant à l'utilisateur
	 * @param {listerCalendriersCallback} callback Fonction rappelée une fois les calendriers listés 
	 */
	CalendrierGestion.prototype.listerMesCalendriers = function(callback) {
		var me = this;
		this.restManager.effectuerRequete("GET", "mescalendriers", { token: me.restManager.getToken() }, function(data) {
			callback(data.resultCode, data.data);
		});
	};

	
	return CalendrierGestion;
});