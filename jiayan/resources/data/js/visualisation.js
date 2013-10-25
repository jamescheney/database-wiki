google.load('visualization', 1, {packages:['corechart', 'map', 'geochart', 'intensitymap', 'gauge', 'treemap']});
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

/**
  DrawGoogle map test


function drawMap() {
	 var mapOptions = {
          center: new google.maps.LatLng(-34.397, 150.644),
          zoom: 8,
          mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(document.getElementById("map_canvas"),
            mapOptions);

}


function loadScript() {
  var script = document.createElement("script");
  script.type = "text/javascript";
  script.src = "http://maps.googleapis.com/maps/api/js?key=AIzaSyAtI5JtBqhE5EgYXgbAxJJITULbC8-4L-4&sensor=true&callback=drawMap";
  document.body.appendChild(script);
}
*/

/** Draw a chart.
 */
function drawChart(type, chartId, title, xSize, ySize, xlabel, ylabels, points) {
	
	var data = new google.visualization.DataTable();
	if (type =='scatter')
	{data.addColumn("number", xlabel);}
	else
	{data.addColumn("string", xlabel);}
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
			
		case 'combo':	   	
	    	chart = new google.visualization.CombChart(document.getElementById(chartId));
	    	break;
		case 'treemap':
	    	chart = new google.visualization.TreeMap(document.getElementById(chartId));
	    	break;
		case 'gauge':
	    	chart = new google.visualization.Gauge(document.getElementById(chartId));
	    	break;
		case 'candlestick':
	    	chart = new google.visualization.CandlestickChart(document.getElementById(chartId));
	    	break;
		case 'pie':
	    	chart = new google.visualization.PieChart(document.getElementById(chartId));
	    	break;
	  case 'geo':
	    	chart = new google.visualization.GeoChart(document.getElementById(chartId));
	    	break;
		case 'column':
	    	chart = new google.visualization.ColumnChart(document.getElementById(chartId));
	    	break; 
	  case 'area':
	  		chart = new google.visualization.AreaChart(document.getElementById(chartId));
	  		break;
	  case 'scatter':
	  		chart = new google.visualization.ScatterChart(document.getElementById(chartId));
	  		break;
	  case 'itmap':
	  		chart = new google.visualization.IntensityMap(document.getElementById(chartId));
	  		break;
	  case 'line':
	       default: 
	    	chart = new google.visualization.LineChart(document.getElementById(chartId));
	    	break;
	   

	};
	
    chart.draw(data, {width: xSize, height: ySize, title: title, is3D:true,
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


