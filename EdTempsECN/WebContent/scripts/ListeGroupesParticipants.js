define(
		[ "RestManager" ],
		function(RestManager) {

			var ListeGroupesParticipants = function(restManager) {
				this.restManager = restManager;
			};

			ListeGroupesParticipants.prototype.getGroupesActifs = function(data) {

				// TODO : ne retourne que les groupes dont les événements sont à
				// afficher

				return data;

			};

			/**
			 * Affiche le bloc "Vos agendas" de l'écran d'accueil avec la liste des groupes de participants
			 * récupérés en base de donneés
			 * 
			 * @param data
			 */
			ListeGroupesParticipants.prototype.afficherBlocVosAgendas = function(data) {

				// TODO : voir pour faire un tri par ordre alphabétique
				
				var str = "";

				// Génération du code html pour afficher la liste des agendas dans le bloc sur la gauche de l'écran
				for ( var i = 0; i < data.groupes.length; i++) {
					var nom = data.groupes[i].nom;
					str += "<span class='afficher_cacher_groupe'><img src='./img/checkbox_on.png' />"
							+ nom + "</span><br/>";
				}

				// Affiche le code html généré
				$("#liste_groupes").html(str);
			};

			return ListeGroupesParticipants;
		});