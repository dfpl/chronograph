
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