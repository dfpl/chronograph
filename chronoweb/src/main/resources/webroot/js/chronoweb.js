
let validationURL;
let captureURL;
let queryURL;
let captureJobList;
let nextPageToken;
let nextResourcePageToken = '';
let resourceTypeLocal;
let resourceLocal;

// Server connection check and Show status
function serverConn(url, circle) {
	$.ajax({
		url: url,
		crossOrigin: true,
	}).done(() => {
		$(circle).removeClass('bg-danger').addClass("bg-success");
	}).fail(() => {

		console.log("abc");
		$(circle).removeClass('bg-success').addClass("bg-danger");
	});
}

function getGammaTableSources(url, sourceContainerID, sourceMenuID){
	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {
		console.log(result);
		$("#" + sourceContainerID).html("");
		for (let key in result) {
			// console.log(result[key]);
			var newItem = $('<a class="dropdown-item" href="#">' + result[key] + '</a>');
			newItem.click(function() {	
				$("#" + sourceMenuID).text($(this).text());
				// getGammaTablePrograms(url, programContainerID, programMenuID);
			});	
			$("#" + sourceContainerID).append(newItem);
		}
	}).fail((result) => {
		console.log(result);
	});
}

function getGammaTablePrograms(url, programContainerID, programMenuID, sourceContainerID, sourceMenuID){
	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {
		
		$("#" + programContainerID).html("");
		for (var key in result) {
			
			var newItem = $('<a class="dropdown-item" href="#">' + result[key] + '</a>');
			newItem.click(function() {	
				$("#" + programMenuID).text($(this).text());
				getGammaTableSources(url + "/" + result[key] + "/link", sourceContainerID, sourceMenuID);
			});	
			$("#" + programContainerID).append(newItem);
		}
	}).fail((result) => {
		console.log(result);
	});
}

function getGammaTableStartTimes(url, timeContainerID, timeMenuID, programContainerID, programMenuID, sourceContainerID, sourceMenuID) {
	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {
		$("#" + timeContainerID).html("");
		for (key in result) {
			var newItem = $('<a class="dropdown-item" href="#">' + result[key] + '</a>');
			newItem.click(function() {	
				$("#" + timeMenuID).text($(this).text());
				getGammaTablePrograms(url+"/"+result[key], programContainerID, programMenuID, sourceContainerID, sourceMenuID);
			});	
			$("#" + timeContainerID).append(newItem);
		}
	}).fail((result) => {
		console.log(result);
	});
}