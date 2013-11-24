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

	// Filtre des prénoms
	$("#filtre_prenom").keyup(function() {
		filtrerText($(this), $('.data-collumn-prenom'));
	});
	// Filtre des Noms
	$("#filtre_nom").keyup(function() {
		filtrerText($(this), $('.data-collumn-nom'));
	});
	// Filtre des Mails
	$("#filtre_mail").keyup(function() {
		filtrerText($(this), $('.data-collumn-mail'));
	});
});

function filtrerText(idFiltreText, classTri) {
	
	// Récupère la valeur du filtre
    var filtreValeur = idFiltreText.val().toLowerCase();
    
    // Pour chaque ligne du tableau, affiche ou cache en fonction du résultat
    var couleurPrecedente = "white";
    if(filtreValeur!="") {
    	classTri.each(function() {
            if ($(this).html().toLowerCase().indexOf(filtreValeur) >= 0) {
            	if (couleurPrecedente=="#E3EDEF") couleurPrecedente = "white";
            	else couleurPrecedente="#E3EDEF";
            	$(this).parent().css("background-color", couleurPrecedente).fadeIn(200);
            } else {
                $(this).parent().fadeOut(200);
            }
        });
    } else {
    	classTri.parents().fadeIn(200);
    }
    
}