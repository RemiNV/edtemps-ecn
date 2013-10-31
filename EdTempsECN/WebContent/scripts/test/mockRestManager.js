define(["mockjax"], function() {
	
	var connectionToken = "tokenUnitTestMockjax";
	var resultCode_Success = 0;
	var resultCode_IdentificationError = 1;
	
	return {
		
		mock: function() {
			$.mockjax({
				url: "identification/connection",
				responseTime: 500,
				contentType: "application/json",
				type: "POST",
				response: function(settings) {
					
					// Seule connexion possible : userID et pass "unitTest"
					if(settings.data.username == "unitTest" && settings.data.password == "unitTest") {
						this.responseText = JSON.stringify({ resultCode: resultCode_Success, message: "", 
							data: {
								token: connectionToken
							}
						});
					}
					else {
						this.responseText = JSON.stringify({ resultCode: resultCode_IdentificationError, message: "", data: null });
					}
				}
			});
			
			console.log("*** Utilisation de mockjax. Les requêtes AJAX jQuery sont interceptées pour les tests unitaires ***");
		}
	};
	
	
});