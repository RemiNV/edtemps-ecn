define([ "RestManager", "jquerymask" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var RechercheSalle = function(restManager) {
		this.restManager = restManager;

		// Variable qui permettent d'accéder facilement aux différents champs du formulaire
		this.date = $("#form_recherche_salle_date");
		this.heureDebut = $("#form_recherche_salle_debut");
		this.heureFin = $("#form_recherche_salle_fin");
		this.capacite = $("#form_recherche_salle_capacite");

		// Liste des matériels disponibles, récupération des valeurs en base de données
		this.listeMaterielDisponible = new Array();
		
		// Liste des matériels sélectionnées par l'utilisateur
		// Quand un matériel est mis dans cette liste, il est enlevé de la liste des matériels disponibles
		this.materielSelectionne = new Array();




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
		this.heureDebut.mask("00:00");
		this.heureFin.mask("00:00");
		this.capacite.mask("0000");

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
			$(this).css({border: "1px solid #dddddd"});
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
		if (this.date.val()=="") {
			this.date.css({border: "1px solid red"});
			valid = false;
		} else {
			this.date.css({border: "1px solid #dddddd"});
		}

		// Validation de l'heure de début
		var decoupageHeureMinute = this.heureDebut.val().split(":");
		if (this.heureDebut.val().length==0 || decoupageHeureMinute[0]>23 || decoupageHeureMinute[1]>59) {
			this.heureDebut.css({border: "1px solid red"});
			valid = false;
		} else {
			this.heureDebut.css({border: "1px solid #dddddd"});
		}

		// Validation de l'heure de fin
		decoupageHeureMinute = this.heureFin.val().split(":");
		if (this.heureFin.val().length==0 || decoupageHeureMinute[0]>23 || decoupageHeureMinute[1]>59) {
			this.heureFin.css({border: "1px solid red"});
			valid = false;
		} else {
			this.heureFin.css({border: "1px solid #dddddd"});
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
		$("#form_chercher_salle_liste_materiel").append(
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