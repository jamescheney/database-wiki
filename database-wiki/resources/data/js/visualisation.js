google.load('visualization', 1, {packages:['corechart']});

function drawColumnChart(chartId, title, xlabel, ylabels, points) {
	var data = new google.visualization.DataTable();
	
	data.addColumn("string", xlabel);
	for(var i = 0; i < ylabels.length; i++)
		data.addColumn("number", ylabels[i]);
	
	data.addRows(points.length);
	
	for(var i = 0; i < points.length; i++) {
		data.setValue(i, 0, points[i].x);
		for(var j = 0; j < ylabels.length; j++)
			data.setValue(i, 1+j, points[i].y[j]);
	}
	
	var chart = new google.visualization.ColumnChart(document.getElementById(chartId));
    chart.draw(data, {width: 400, height: 240, title: title,
        hAxis: {title: xlabel, titleTextStyle: {color: 'red'}},
        //vAxis: {title: ylabel, titleTextStyle: {color: 'red'}}
       });	
}

function drawMap(mapId, points) {	
	var geocoder;
	var map;
    geocoder = new google.maps.Geocoder();
    var options = {
      zoom: 6,
      center: new google.maps.LatLng(0, 0),
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById(mapId), options);
	 
    // Google currently (November 2011) has a quota of 2500 geocode requests per day per IP address.
    // There is also an unspecified restriction on the rate at which requests can be made.
    // To avoid hitting the rate restriction we use setTimeout to ensure that each
    // request is at least a second apart.
    
    // HACK: JavaScript's handling of closures is stupid so we have to
    // beta-expand the function passed to setTimeout. This has the effect
    // of converting points[i] from a reference to a value.
    for(var i = 0; i < Math.min(5, points.length); i++) {
		setTimeout(
			(function (address) {
				 return function () {
					 geocoder.geocode( { 'address': address}, function(results, status) {
						 if (status == google.maps.GeocoderStatus.OK) {
							 var pos = results[0].geometry.location;
							 map.setCenter(pos);
							 var marker = new google.maps.Marker({
								 map: map, 
								 position: pos,
								 title: address
							 });
							 google.maps.event.addListener(marker, 'click', function() {
								 map.setCenter(pos);
								 map.setZoom(8);
							 });
						 } else {
							 //alert("Geocode was not successful for the following reason: " + status);
						 }
					 }
				 )}})(points[i]), 1000);
	}
}


