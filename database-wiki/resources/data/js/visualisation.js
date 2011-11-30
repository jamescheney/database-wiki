google.load('visualization', 1, {packages:['corechart', 'map']});

/** Use the Google visualization API to draw a map
 *
 * @chartId is the id of the DIV to draw it in
 * @addresses is an array of addresses
 */
function drawMap(chartId, addresses) {
	var data = new google.visualization.DataTable();
	
	data.addColumn('string', 'address');
	data.addRows(addresses.length);
	
	for(var i = 0; i < addresses.length; i++) {
		data.setCell(i, 0, addresses[i]);
	}
	
	var map = new google.visualization.Map(document.getElementById(chartId));
	map.draw(data, {mapType: 'normal', enableScrollWheel: true, showTip: true});
}

/** Draw a chart.
 */
function drawChart(type, chartId, title, xSize, ySize, xlabel, ylabels, points) {
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
	
	var chart;
	switch (type) {
		case 'pie':
	    	chart = new google.visualization.PieChart(document.getElementById(chartId));
	    	break;
		case 'column':
	    default:
	    	chart = new google.visualization.ColumnChart(document.getElementById(chartId));
	    	break;
	};
	
    chart.draw(data, {width: xSize, height: ySize, title: title,
        hAxis: {title: xlabel, titleTextStyle: {color: 'black'}}
        //vAxis: {title: ylabel, titleTextStyle: {color: 'black'}}
    });	
}

// This uses the Google Maps API directly.
// It's simpler to use the visualization API.
function drawMapRaw(mapId, points) {	
	var geocoder;
	var map;
    geocoder = new google.maps.Geocoder();
    var options = {
      zoom: 3,
      center: new google.maps.LatLng(0, 0),
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById(mapId), options);
	 
    // Google currently (November 2011) has a quota of 2500 geocode requests per day per IP address.
    // There is also an unspecified restriction on the rate at which requests can be made.
    // To avoid hitting the rate restriction we use setTimeout to ensure that there is a delay
    // between consecutive requests apart.
    
    // HACK: JavaScript's handling of closures is stupid so we have to
    // beta-expand the function passed to setTimeout. This has the effect
    // of converting points[i] from a reference to a value.
    for(var i = 0; i < Math.min(15, points.length); i++) {
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
							 });
						 } else {
							 //alert("Geocode was not successful for the following reason: " + status);
						 }
					 }
				 )}})(points[i]), 250*i);
	}
}


