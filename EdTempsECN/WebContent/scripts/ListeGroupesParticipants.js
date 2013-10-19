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

			ListeGroupesParticipants.prototype.afficherBlocVosAgendas = function(data) {

				var str = "";

				// Génération du code html pour afficher la liste des agendas dans le bloc sur la gauche de l'écran
				for ( var i = 0; i < data.groupes.length; i++) {
					var id = data.groupes[i].id;
					var nom = data.groupes[i].nom;
					str += "<div id='afficheGroupe"
							+ id
							+ "' class='afficher_cacher_groupe' onclick=''><img src='./img/checkbox_on.png' />"
							+ nom + "</div>";
				}

				// Affiche le code html généré
				$("#liste_groupes").html(str);
			};

			return ListeGroupesParticipants;
		});