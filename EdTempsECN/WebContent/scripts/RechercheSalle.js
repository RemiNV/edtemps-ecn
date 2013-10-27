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

		// Liste des materiels rangés dans un objet référencé par id
		this.listeMateriel = new Array();


		// TODO : à récupérer en base de données
		var ordinateur = new Object();
		ordinateur.id = 0;
		ordinateur.nom = "Ordinateur";
		ordinateur.quantite = 0;
		this.listeMateriel.push(ordinateur);
		
		var video = new Object();
		video.id = 1;
		video.nom = "Vidéo-projecteur";
		video.quantite = 0;
		this.listeMateriel.push(video);
		
		var cafe = new Object();
		cafe.id = 2;
		cafe.nom = "Cafetière";
		cafe.quantite = 0;
		this.listeMateriel.push(cafe);

		// Ecrit de la liste des matériels disponibles
		this.ecritListeMateriel();

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

		// Ajout du datepicker sur le champ date
		this.date.datepicker({
			showAnim : 'slideDown',
			showOn: 'button',
			buttonText: "Calendrier",
			buttonImage: "img/datepicker.png", // image pour le bouton d'affichage du calendrier
			buttonImageOnly: true, // affiche l'image sans bouton
			monthNamesShort: [ "Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jui", "Aou", "Sep", "Oct", "Nov", "Dec" ],
			monthNames: [ "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" ],
			dayNamesMin: [ "Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa" ],
			dayNames: [ "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" ],
			gotoCurrent: true,
			prevText: "Précédent",
			nextText: "Suivant",
			firstDay: 1
		});

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

		// Affectation d'une méthode au clique sur les différents champs
		$("#form_recherche_salle_date, #form_recherche_salle_debut, #form_recherche_salle_fin, #form_recherche_salle_capacite").click(function() {
			$(this).css({border: "1px solid black"});
		});
		
		// Affiche la boîte dialogue de recherche d'une salle libre
		$("#form_chercher_salle").dialog({
			width: 440,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
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
			this.date.css({border: "1px solid black"});
		}

		// Validation de l'heure de début
		var decoupageHeureMinute = this.heureDebut.val().split(":");
		if (this.heureDebut.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.heureDebut.css({border: "1px solid red"});
			valid = false;
		} else {
			this.heureDebut.css({border: "1px solid black"});
		}

		// Validation de l'heure de fin
		decoupageHeureMinute = this.heureFin.val().split(":");
		if (this.heureFin.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.heureFin.css({border: "1px solid red"});
			valid = false;
		} else {
			this.heureFin.css({border: "1px solid black"});
		}
		
		// Validation de la capacité
		if (isNaN(this.capacite.val())) {
			this.capacite.css({border: "1px solid red"});
			valid = false;
		} else {
			this.capacite.css({border: "1px solid black"});
		}
		
		// Validation des quantités de matériel
		$(".quantite input[type=number]").each(function() {
			if (isNaN($(this).val()) || $(this).val()<0 || $(this).val()=="") {
				$(this).css({border: "1px solid red"});
				valid = false;
			} else {
				$(this).css({border: "1px solid white"});
			}
		});

		return valid;

	};
	
	
	/**
	 * Ecrit la liste des matériels
	 */
	RechercheSalle.prototype.ecritListeMateriel = function() {

		// Préparation du code html
		var str = "";
		for (var i=0, maxI=this.listeMateriel.length ; i<maxI ; i++) {
			str += "<tr>";
			str += "<td class='libelle'>" + this.listeMateriel[i].nom + "</td>";
			str += "<td class='quantite'><input type='number' id='' name='' value='" + this.listeMateriel[i].quantite + "' /></td>";
			str += "</tr>";
		}
		
		// Ajout du texte dans la liste de matériel
		$("#form_chercher_salle_liste_materiel table").append(str);

		// Ajout des masques sur les quantités de matériel
		$(".quantite input[type=number]").each(function() {
			$(this).mask("0000");
			$(this).click(function() {
				$(this).css({border: "1px solid white"});
			});
		});


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