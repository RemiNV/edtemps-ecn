define([ "RestManager", "jquerymask" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var RechercheSalle = function(restManager) {
		this.restManager = restManager;

		this.date;
		this.heureDebut;
		this.heureFin;
		this.capacite;
		this.materielSelectionne = new Array();
		
		this.listeMaterielDisponible = new Array();
		// TODO : à récupérer en base de données
		var ordinateur = new Object();
		ordinateur.id = 1;
		ordinateur.nom = "Ordinateur";
		this.listeMaterielDisponible.push(ordinateur);
		var video = new Object();
		video.id = 2;
		video.nom = "Vidéo-projecteur";
		this.listeMaterielDisponible.push(video);
	};

	/**
	 * Initialise et affiche la boîte de dialogue de recherche d'une salle libre
	 */
	RechercheSalle.prototype.init = function() {
		var me = this;

		// Ajout des masques aux différents champs
		$("#form_recherche_salle_debut").mask("00:00");
		$("#form_recherche_salle_fin").mask("00:00");
		$("#form_recherche_salle_capacite").mask("0000");

		// Affectation d'une méthode au clic sur le bouton "Rechercher"
		$("#form_chercher_salle_valid").click(function() {
			// Si le formulaire est valide, la requête est effectuée
			if (me.validationFormulaire()) {
				this.effectuerRequete();
			}

		});
		
		// Affectation d'une méthode au clic sur le bouton "Annuler"
		$("#form_chercher_salle_annuler").click(function() {
			$("#form_chercher_salle").dialog("close");
		});
		
		// Affectation d'une méthode au clic sur le bouton "Ajouter un matériel"
		$("#form_chercher_salle_ajout").click(function() {
			me.ajouterMateriel();
		});

		// Affectation d'une méthode au clique sur les différents champs
		$("#form_recherche_salle_date, #form_recherche_salle_debut, #form_recherche_salle_fin, #form_recherche_salle_capacite").click(function() {
			$(this).css({border: "1px solid gray"});
		});
		
		// Affiche la boîte dialogue de recherche d'une salle libre
		$("#form_chercher_salle").dialog({
			width: 440,
			modal: true,
			show: {
				effect: "fade",
				duration: 500
			},
			hide: {
				effect: "explode",
				duration: 300
			}
		});

	};

	/**
	 * Méthode qui vérifie que le formulaire est correct
	 * 
	 * @return VRAI si le formulaire est valide et FAUX sinon
	 */
	RechercheSalle.prototype.validationFormulaire = function() {
		var valid = true;

		// Validation de la date
		this.date = $("#form_recherche_salle_date").val();
		if (date=="") {
			$("#form_recherche_salle_date").css({border: "2px solid red"});
			$("label[for='form_recherche_salle_date']").css({color: "red"});
			valid = false;
		}

		// Validation de l'heure de début
		var heureDebut = jQuery.trim($("#form_recherche_salle_debut").val());
		var decoupageHeureMinute = heureDebut.split(":");
		if (heureDebut.length==0 || decoupageHeureMinute[0]>23 || decoupageHeureMinute[1]>59) {
			$("#form_recherche_salle_debut").css({border: "2px solid red"});
			$("label[for='form_recherche_salle_debut']").css({color: "red"});
			valid = false;
		}

		// Validation de l'heure de fin
		var heureFin = jQuery.trim($("#form_recherche_salle_fin").val());
		decoupageHeureMinute = heureFin.split(":");
		if (heureFin.length==0 || decoupageHeureMinute[0]>23 || decoupageHeureMinute[1]>59) {
			$("#form_recherche_salle_fin").css({border: "2px solid red"});
			$("label[for='form_recherche_salle_fin']").css({color: "red"});
			valid = false;
		}
		
		return valid;

	};
	
	/**
	 * Méthode permettant d'ajouter une ligne dans le tableau des matériels
	 */
	RechercheSalle.prototype.ajouterMateriel = function() {

		var materiel = new Object();
		this.materielSelectionne.push(materiel);
		
		// Ajout d'une ligne dans la boîte de dialogue
		$("#form_chercher_salle_table").append(		
			"<tr>" +
				"<td><label for='form_recherche_salle_mat_1'>Materiel 1</label></td>" +
				"<td>" +
					"<input type='number' name='form_recherche_salle_mat_1' id='form_recherche_salle_mat_1' style='width: 50px;' class='ui-widget-content ui-corner-all' value='1' />" +
					"<select id='form_recherche_salle_mat_1_liste'>" +
						"<option value='undefined'>---</option>" +
						"<option value=''>Ordinateur</option>" +
						"<option value=''>Vidéo-projecteur</option>" +
					"</select>" +
					"<img src='img/supprimer.png' title='Supprimer' />" +
				"</td>" +
			"</tr>"
		);
	
	};
	
	/**
	 * Méthode qui effectue la requête
	 */
	RechercheSalle.prototype.effectuerRequete = function() {
		alert('Requete au serveur pour récupérer la liste des salles correspondantes');
	};
	
	/**
	 * Méthode qui affiche le résultat
	 */
	RechercheSalle.prototype.afficherResultat = function() {
		alert('Affiche le résultat');
	};

	return RechercheSalle;

});