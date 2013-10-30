define(["RestManager", "GroupeGestion", "jqueryquicksearch", "jqueryui", "jquerymultiselect", "jquery"], function(RestManager, GroupeGestion) {
	
	/**
	 * Cet écran est associé au HTML templates/page_parametres.html.
	 * Il affiche la page de paramètres, avec ses différents onglets (abonnements, mes agendas, mes groupes...) */
	var EcranParametres = function(restManager) {
		this.restManager = restManager;
		this.groupeGestion = new GroupeGestion(this.restManager);
	};
	
	EcranParametres.prototype.init = function() {
		
		// Initialisaion de la navigation par tabs
		$("#tabs").tabs();
		
		// Initialisation des dialog
		$("#dialog_export").dialog({
			autoOpen: false,
			modal: true,
			width: 600
		});
		
		// Listeners
		$("#btn_parametres_retour").click(function() {
			Davis.location.assign("agenda");
		});
		
		this.initMesAbonnements();
	};

	EcranParametres.prototype.initMesAbonnements = function() {

		// Remplir les "abonnements" et "non abonnments" dans le MultiSelect
		this.groupeGestion.queryAbonnementsEtNonAbonnements(function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				// Variable recevant progressivement le code HTML à ajouter à l'élément MultiSelect
				var html = "";
				// Variable temporaire contenant le groupe en cours de lecture
				var gpe; 
				// Parcourt des groupes auxquels l'utilisateur est abonné 
				for (var i = 0, maxI=data.groupesAbonnements.length ; i < maxI ; i++) {
					gpe = data.groupesAbonnements[i];
					html += '<option value="' + gpe.id + '"';
					if (gpe.estCours) {
						html += ' disabled="disabled"';
					}
					html += '>' + gpe.nom + '</option>';
				}
				// Parcourt des groupes auxquels l'utilisateur n'est pas abonné
				for (var i = 0, maxI=data.groupesNonAbonnements.length ; i < maxI ; i++) {
					gpe = data.groupesNonAbonnements[i];
					html += '<option value="' + gpe.id + '" selected="selected">' + gpe.nom + '</option>';
				}

				// Affichage 
		/*Test
		html = "" +
		"<select id='optgroup' multiple='multiple'>" +
		  "<optgroup label='Friends'>" +
			  "<optgroup label='Enes'>" +
			    "<option value='5'>Palpatine</option>" +
			    "<option value='6' disabled>Darth Vader</option>" +
			  "</optgroup>" +
			  "<optgroup label='mies'>" +
			    "<option value='4'>Palpatine</option>" +
			    "<option value='8' disabled>Darth Vader</option>" +
			  "</optgroup>" +
		  "</optgroup>" +
		  "<optgroup label='Enemies'>" +
		    "<option value='3'>Palpatine</option>" +
		    "<option value='4' disabled>Darth Vader</option>" +
		  "</optgroup>" +
		"</select>" ;
		 */
				$("#select-abonnements").html(html);
				
				// Paramètres de l'objet multiSelect
				$("#select-abonnements").multiSelect({
					selectableHeader: "<h3>Mes abonnements : </h3><input type='text' class='select-abonnements-filtre' autocomplete='off' placeholder='Filtrer...'>",
					selectionHeader: "<h3>Agendas disponibles : </h3><input type='text' class='select-abonnements-filtre' autocomplete='off' placeholder='Filtrer...'>",
					afterInit: function(ms){
						var me = this,
						$selectableSearch = me.$selectableUl.prev(),
						$selectionSearch = me.$selectionUl.prev(),
						selectableSearchString = '#'+me.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)',
						selectionSearchString = '#'+me.$container.attr('id')+' .ms-elem-selection.ms-selected';

						me.qs1 = $selectableSearch.quicksearch(selectableSearchString);
						me.qs2 = $selectionSearch.quicksearch(selectionSearchString);
				    },
					afterSelect: function(idgroupe){
						me.groupeGestion.seDesabonner(idgroupe, function(resultCode) {
							// En cas d'erreur, on affiche un message et replace l'élément sélectionné dans les abonnements de l'utilisateur
							if(resultCode != RestManager.resultCode_Success) {
								window.showToast("Le désabonnement a échoué ...");
								var idElementSelectable = "#" + idgroupe + "-selectable";
								var idElementSelection = "#" + idgroupe + "-selection";
								$(idElementSelection).css('display','none');
								$(idElementSelectable).css('display','list-item');
							}
						});
					},
					afterDeselect: function(idgroupe){
						 me.groupeGestion.sAbonner(idgroupe, function(resultCode) {
							// En cas d'erreur, on affiche un message et replace l'élément sélectionné dans les "Agendas disponibles"
							if(resultCode != RestManager.resultCode_Success) {
								window.showToast("L'abonnement a échoué ...");
								var idElementSelectable = "#" + idgroupe + "-selectable";
								var idElementSelection = "#" + idgroupe + "-selection";
								$(idElementSelectable).css('display','none');
								$(idElementSelection).css('display','list-item');
							}
						});
					}
				});
				
			}
			
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de chargement des groupes (auxquels vous êtes abonnés ou non) ; vérifiez votre connexion.");
			}

			else {
				window.showToast(resultCode + " Erreur de chargement des groupes (auxquels vous êtes abonnés ou non) ; votre session a peut-être expiré ?");
			}
		});
		
		// Ajout des listeners
		var me = this;
		$("#btn_export").click(function(event) {
			me.restManager.effectuerRequete("GET", "ical/token", { token: me.restManager.getToken() }, function(response) {
				
				if(response.resultCode == RestManager.resultCode_Success) {
					// Remplissage de la dialog d'export
					var urlIcal = document.location.origin + document.location.pathname + "ical/get?token=" + response.data.token;
					$("#dialog_export #txt_url_agenda_ical").html(urlIcal);
					$("#dialog_export #lien_export_gmail").attr("href", "http://www.google.com/calendar/render?cid=" + encodeURIComponent(urlIcal));
					
					var urlSansProtocole = urlIcal.substring(location.protocol.length);
					
					$("#dialog_export #lien_export_ical").attr("href", "webcal:" + urlSansProtocole);
					
					// Affichage de la dialog initialisée
					$("#dialog_export").dialog("open");
				}
				else if(response.resultCode == RestManager.resultCode_NetworkError) {
					window.showToast("Erreur de récupération de votre URL ICal : vérifiez votre connexion");
				}
				else {
					window.showToast("Erreur de récupération de votre URL ICal");
				}
				
			});
		});

	};
	
	return EcranParametres;
});