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
					html += '<option value="' + gpe.id + '">' + gpe.nom + '</option>';
				}
				// Parcourt des groupes auxquels l'utilisateur n'est pas abonné
				for (var i = 0, maxI=data.groupesNonAbonnements.length ; i < maxI ; i++) {
					gpe = data.groupesNonAbonnements[i];
					html += '<option value="' + gpe.id + '" selected="selected">' + gpe.nom + '</option>';
				}

				// Affichage 
				html += '<option value="test4">Test 4</option>';
				html += '<option value="test5" selected="selected">Test 5</option>';

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
					afterSelect: function(){
						this.qs1.cache();
						this.qs2.cache();
					},
					afterDeselect: function(){
						this.qs1.cache();
						this.qs2.cache();
					}
				});
				
			}
			
			else if(resultCode == RestManager.resultCode_NetworkError) {
				alert("Erreur de chargement des groupes (auxquels vous êtes abonnés ou non) ; vérifiez votre connexion.");
			}

			else {
				alert(resultCode + " Erreur de chargement des groupes (auxquels vous êtes abonnés ou non) ; votre session a peut-être expiré ?");
			}
		});

	};
	
	return EcranParametres;
});