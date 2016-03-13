$(document).ready(function() {
    //$('body').hide().fadeIn(5000);

  	var weatherAPI = "/content/weather.json";
	console.log("weather request start");
    var items = [];
    $.getJSON( weatherAPI, function( data ) {
	$.each( data.forecast, function( key, val ) {
    items.push( "<div class='weather-item' id='" + key + "'><p class='weather-day'>" + val.dayname + "</p><div class='icon cloudy'><div class='cloud'></div><div class='cloud'></div></div><p>" +val.type + "</p><p class='weather-degrees'>" +val.min + " | " +val.max + "</p></div>" );
//    items.push( "<div class='weather-item' id='" + key + "'><h3 class='weather-day'>" +val.dayname + "</h3><h4>" +val.type + "</h4><h3>" +val.min + " | " +val.max + "</h3></div>" );
  	});
    })

  .done(function() {
	$( "<div/>", {
    "class": "weather-container",
    html: items.join( "" )
  }).appendTo( ".weather-panel" );
  })
  .fail(function(jqxhr, textStatus, error ) {
    var err = textStatus + ", " + error;
    console.log( "Request Failed: " + err );
  })
  .always(function() {
    console.log( "weather request complete" );
  });

});
