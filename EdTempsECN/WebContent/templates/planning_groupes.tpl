<table class="tbl_planning_groupes">
	<tr class="planning_groupes_header">
		<th class="planning_groupes_num_semaine" rowspan="2">
			Semaine <span class="semaine"></span>
		</th>
		<%
		for(var i=0; i<6; i++) {
		%>
		<th><%= jours[i] %></th>
		<%
		}
		%>
	</tr>
	<tr class="planning_groupes_ligne_horaires">
		<%
		for(var i=0; i<6; i++) {
		%>
		<td>
			<div class="horaire_creneau">10</div>
			<div class="horaire_creneau">12</div>
			<div class="horaire_creneau">14</div>
			<div class="horaire_creneau">16</div>
			<div class="horaire_creneau">18</div>
		</td>
		<%
		}
		%>
	</tr>
	<%
	for(var i=0,max=groupes.length; i<max; i++) {
	%>
	<tr style="height: <%= 100/(groupes.length + 1) %>%;">
		<td class="case_nom_groupe"><%= groupes[i].nom %></td>
		<%		
		for(var j=0; j<6; j++) {
		%>
		<td class="jour">
			<%
			for(var k=0; k<6; k++) {
			%>
			<div class="division_creneau"></div>
			<%
			}
			%>
		</td>
		<%
		}
		%>
	</tr>
	<%
	}
	%>
	
</table>