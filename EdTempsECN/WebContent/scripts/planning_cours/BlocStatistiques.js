/**
 * Module d'affichage des statistiques dans la barre de gauche de l'écran d'accueil
 * @module BlocStatistiques
 */
define(["underscore", "text!../../templates/bloc_statistiques.tpl", "jquery"], function(_, templateBlocStatistiques) {
	
	/**
	 * @constructor
	 * @alias BlocStatistiques 
	 */
	var BlocStatistiques = function(jqBloc) {
		this.jqBloc = jqBloc;
		this.groupes = null;
		
		/** Clé : type de cours, valeur : objet indexé par idGroupe, dont les valeurs sont "actuel" et "prevu".
		 * Ex : statistiques[typeCours][idGroupe].actuel, statistiques[typeCours][idGroupe].prevu */
		this.statistiques = {
				'TD': {},
				'TP': {},
				'CM': {},
				'DS': {}
		};
		this.template = _.template(templateBlocStatistiques);
	};
	
	/**
	 * Définit les groupes affichés dans les statistiques
	 * @param {integer[]} idsGroupes IDs des groupes
	 * @param {integer[]} nomsGroupes Noms des groupes, doit avoir la même taille que idsGroupes
	 */
	BlocStatistiques.prototype.setGroupes = function(idsGroupes, nomsGroupes) {
		this.groupes = new Object();
		for(var i=0,maxI=idsGroupes.length; i<maxI; i++) {
			this.groupes[idsGroupes[i]] = nomsGroupes[i];
		}
	};
	
	BlocStatistiques.prototype.draw = function() {
		var html = this.template({ statistiques: this.statistiques, groupes: this.groupes });
		this.jqBloc.find("#contenu_statistiques").html(html);
	};
	
	return BlocStatistiques;
});