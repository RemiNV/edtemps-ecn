define(["jqueryquicksearch", "jqueryui", "jquerymultiselect"], function() {
	
	var EcranParametres = function() {
		
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
	};
	
	return EcranParametres;
});