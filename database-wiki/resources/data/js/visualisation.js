google.load('visualization', 1, {packages:['corechart']});

function drawColumnChart(title, xlabel, ylabel, points) {
	var data = new google.visualization.DataTable();
	
	data.addColumn("string", xlabel);
	data.addColumn("number", ylabel);
	
	data.addRows(points.length);
	
	for(var i = 0; i < points.length; i++) {
		data.setValue(i, 0, points[i].x);
		data.setValue(i, 1, points[i].y);
	}
	
	var chart = new google.visualization.ColumnChart(document.getElementById("chart"));
    chart.draw(data, {width: 400, height: 240, title: title,
        hAxis: {title: xlabel, titleTextStyle: {color: 'red'}},
        vAxis: {title: ylabel, titleTextStyle: {color: 'red'}}
       });	
}

function drawMap(points) {	
	var geocoder;
	var map;
    geocoder = new google.maps.Geocoder();
    var myOptions = {
      zoom: 4,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("map"), myOptions);
	 
    // Google currently (November 2011) has a quota of 2500 geocode requests per day per IP address.
    // There is also an unspecified restriction on the rate at which requests can be made.
    // To avoid hitting the rate restriction we use setTimeout to ensure that each
    // request is at least a second apart.
    
    // HACK: JavaScript's handling of closures is stupid so we have to
    // beta-expand the function passed to setTimeout.
    for(var i = 0; i < 5; i++) {
    	// 
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
							 alert("Geocode was not successful for the following reason: " + status);
						 }
					 }
				 )}})(points[i]), 1000);
	}
}


