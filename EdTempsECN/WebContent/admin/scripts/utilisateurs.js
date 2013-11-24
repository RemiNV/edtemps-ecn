$(document).ready(function() {
	
	// Pré-sélectionne les types dans les listes déroulantes
	$.each($(".select_type"), function() {
		var idType = $(this).attr("data-type-id");
		if (idType!=0) {
			$(this).find("option[value="+idType+"]").prop('selected', true);
		}
		// Lors du changement d'une liste déroulante, la valeur est mise à jour en base de données
		$(this).change(function() {
			if (confirm("Etes-vous sur de vouloir modifier ce type d'utilisateur ?")) {
				$(this).parents("form").submit();
			}
		});
	});

});