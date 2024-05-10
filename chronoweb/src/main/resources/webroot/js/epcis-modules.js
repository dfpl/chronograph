const href = window.location.href;
let hrefArr = href.split("/");
let baseURL = hrefArr[0] + "//" + hrefArr[2] + ":8080/epcis";

let validationURL;
let captureURL;
let queryURL;
let captureJobList;
let nextPageToken;
let nextResourcePageToken = '';
let resourceTypeLocal;
let resourceLocal;

// Server connection check and Show status
function serverConn(url, urlToShow) {
	$.ajax({
		url: url,
		crossOrigin: true,
	}).done(() => {
		$("#serverResp").removeClass('bg-danger').addClass("bg-success");
		$("#serverEndpoint").html(urlToShow);
	}).fail(() => {
		$("#serverResp").removeClass('bg-success').addClass("bg-danger");
		$("#serverEndpoint").html("");
	});

	$.ajax({
		url: baseURL + "/statistics",
		crossOrigin: true,
	}).done((result) => {

		$("#numOfEvents").html(result.events);
		$("#numOfVocabularies").html(result.vocabularies);
		$("#epcs_in_events").html(result.epcs_in_events);
		$("#epcs_in_vocabularies").html(result.epcs_in_vocabularies);
		$("#bizSteps").html(result.bizSteps);
		$("#bizLocations_in_events").html(result.bizLocations_in_events);
		$("#bizLocations_in_vocabularies").html(result.bizLocations_in_vocabularies);
		$("#readPoints_in_events").html(result.readPoints_in_events);
		$("#readPoints_in_vocabularies").html(result.readPoints_in_vocabularies);
		$("#dispositions").html(result.dispositions);
		$("#eventTypes").html(result.eventTypes);
		$("#captureJob").html(result.captureJobs);
		$("#subscription").html(result.subscriptions);
		$("#namedQuery").html(result.namedQueries);
	});
}

function searchResources(resourceType, contentType, invalidatePaging) {
	if (resourceType != null) {
		resourceTypeLocal = resourceType;
	}
	if (invalidatePaging == true) {
		nextResourcePageToken = '';
	}


	var url;
	if (nextResourcePageToken === '') {
		url = baseURL + "/" + resourceTypeLocal + "?perPage=6";
	} else {
		url = baseURL + "/" + resourceTypeLocal + "?perPage=6&nextPageToken=" + nextResourcePageToken;
	}

	$.ajax({
		type: 'GET',
		url: url,
		contentType: contentType,
		crossOrigin: true
	}).done((result, textStatus, request) => {

		$('#eventsOption').attr("disabled", false);
		$('#vocabulariesOption').attr("disabled", false);
		$('#eventsOption').attr("checked", false);
		$('#vocabulariesOption').attr("checked", false);


		$("#resourceList").empty();


		if (contentType.includes('json')) {
			for (key in result['member']) {
				var resource = result['member'][key];
				var newItem = $("<div class='form-check'><input class='form-check-input resource' type='radio' name='resource' value='" + resource + "'><label class='form-check-label' for='flexRadioDefault2'>" + resource + "</label></div>");
				$('#resourceList').append(newItem);


				let link = request.getResponseHeader('Link');  // get 'Link' header
				if (link != null) {
					nextResourcePageToken = link;
					$('#nextResourcePage').removeClass('invisible');
				}
				if (link == null) {
					nextPageToken = null;
					$('#nextResourcePage').addClass('invisible');
				}
			}
		} else if (contentType.includes('xml')) {
			if(resourceTypeLocal == null || resourceTypeLocal === '' || resourceTypeLocal.length == 0)
				return;
			
			$.each($(result).find(resourceTypeLocal.substring(0, resourceTypeLocal.length-1)), function(key, value) {
				var resource = $(value).text()
				var newItem = $("<div class='form-check'><input class='form-check-input resource' type='radio' name='resource' value='" + resource + "'><label class='form-check-label' for='flexRadioDefault2'>" + resource + "</label></div>");
				$('#resourceList').append(newItem);


				let link = request.getResponseHeader('Link');  // get 'Link' header
				if (link != null) {
					nextResourcePageToken = link;
					$('#nextResourcePage').removeClass('invisible');
				}
				if (link == null) {
					nextPageToken = null;
					$('#nextResourcePage').addClass('invisible');
				}
			});
		}


		$('input[type=radio][name="resource"]').change(function() {
			nextPageToken = '';
			resourceLocal = $(this).attr('value');
			var resourceURL = url = baseURL + "/" + resourceTypeLocal + "/" + encodeURIComponent(resourceLocal);
			$.ajax({
				type: 'GET',
				url: resourceURL,
				contentType: contentType,
				crossOrigin: true
			}).done((result, textStatus, request) => {
				$('#eventsOption').attr("disabled", true);
				$('#vocabulariesOption').attr("disabled", true);
				$('#eventsOption').attr("checked", false);
				$('#vocabulariesOption').attr("checked", false);
				if (contentType.includes('json')) {
					if (result['@set'].includes('events')) {
						$('#eventsOption').attr("disabled", false);
					}

					if (result['@set'].includes('vocabularies')) {
						$('#vocabulariesOption').attr("disabled", false);
					}
				} else if (contentType.includes('xml')) {
					
					if($(result).text().includes('events')){
						$('#eventsOption').attr("disabled", false);
					}
					if($(result).text().includes('vocabularies')){
						$('#vocabulariesOption').attr("disabled", false);
					}				
				}
			});
		});

	}).fail((result) => {
		// result formatting
		result = formatXml(result.responseText);
		editor_resp.setValue(result);

		$("#subResultButton").addClass('d-none');
	});

}


// Reset database resourses
function resetDB() {
	let responseToast = $("#responseToast");
	let toastText;
	$("#resetIcon").addClass("fa-spin");  // icon spins

	$.ajax({
		type: "DELETE",
		url: baseURL,
		crossOrigin: true,
	}).done((result) => {
		toastText = "200 OK";
		responseToast.find(".toast-body").html(toastText);
		responseToast.toast("show");
		setTimeout(() => {
			$("#resetIcon").removeClass("fa-spin"); // stop spinning
		}, 1000);
	}).fail((result) => {
		toastText = result.responseText;
	})

	// show the repository status in 5 seconds (because the server resourses are updated every 5 sec)
	//setTimeout(() => {
	//	responseToast.find(".toast-body").html(toastText);
	//	responseToast.toast("show");

	//	$.ajax({
	//		url: baseURL + "/statistics",
	//		crossOrigin: true,
	//	}).done((result) => {

	///		$("#numOfEvents").html(result.events);
	////		$("#numOfVocabularies").html(result.vocabularies);
	//		$("#epcs").html(result.epcs);
	//		$("#bizSteps").html(result.bizSteps);
	//		$("#bizLocations").html(result.bizLocations);
	///		$("#readPoints").html(result.readPoints);
	//		$("#dispositions").html(result.dispositions);
	//		$("#eventTypes").html(result.eventTypes);
	//	})
	//	$("#resetIcon").removeClass("fa-spin"); // stop spinning
	// }, 5000);
}


// Validate editor contents
function isValid(format) {
	$.ajax({
		type: "POST",
		url: validationURL,
		data: editor.getValue(),
		contentType: "application/" + format + "; charset=utf-8",
		crossOrigin: true
	}).done((result) => {
		$("#docValResp")
			.val("Valid Document")
			.hide()
			.fadeIn('slow');
	}).fail((result) => {
		$("#docValResp")
			.val(result.responseText)
			.hide()
			.fadeIn('slow');
	}).always((result) => {
		$("#status").removeClass("text-warning text-success text-danger Blink").addClass("text-secondary");
	});
}


function monitor() {
	let url = baseURL.replace("http", "ws") + "/uiSocket";

	socket = new WebSocket(url);

	socket.onopen = function(e) {
		$("#monitorButton").attr("disabled", true);
		$("#connStatus").val("WebSocket established");
		$("#status").removeClass("text-warning Blink").addClass("text-success Blink");
	};

	socket.onmessage = function(event) {
		console.log(event.data);
		let resultXML = new DOMParser().parseFromString(event.data, 'application/xml');
		subId = $(resultXML).find('subscriptionID').text();  // in soapSubscribe.html
		let xmlString = formatXml(new XMLSerializer().serializeToString(resultXML));// update editor contents
		$("#subscriptionID").val(subId);
		$("#timeReceived").val(new Date().toLocaleString());
		editor_resp.setValue(xmlString);
		foldEventVoca(editor_resp, 'xml');

	};

	socket.onclose = function(event) {
		console.log(event);
	};

	socket.onerror = function(error) {
		console.log(error);
	};
}



function pagingSOAPQuery() {
	let httpMethod;
	let httpBody;
	if (nextPageToken == null) {
		queryURL = baseURL + "/query?PerPage=20";
		httpMethod = "POST";
		httpBody = editor.getValue();
	} else {
		queryURL = baseURL + "/events?PerPage=20&NextPageToken=" + nextPageToken;
		httpMethod = "GET";
		httpBody = ""
	}
	$.ajax({
		type: httpMethod,
		url: queryURL,
		data: httpBody,
		contentType: "application/xml; charset=utf-8",
		crossOrigin: true
	}).done((result, textStatus, request) => {
		let link = request.getResponseHeader('Link');  // get 'Link' header
		if (link != null) {

			nextPageToken = link;
			$('#nextPage').removeClass('invisible');
			$('#hasNext').removeClass('invisible');
		}
		if (link == null) {
			nextPageToken = null;
			$('#nextPage').addClass('invisible');
			$('#hasNext').addClass('invisible');
		}

		// result formatting

		let xmlString = formatXml(new XMLSerializer().serializeToString(result));// update editor contents
		editor_resp.setValue(xmlString);
		// 'fold' editor conetnts
		foldEventVoca(editor_resp, 'xml');

		if ($(result).find("query\\:SubscribeResult").length) {
			const parser = new DOMParser();
			const xml = parser.parseFromString(editor.getValue(), 'application/xml');
			subId = $(xml).find('subscriptionID').text();  // in soapSubscribe.html

			$("#subResultButton").removeClass('d-none');
		} else {
			$("#subResultButton").addClass('d-none');
		}

	}).fail((result) => {
		// result formatting
		result = formatXml(result.responseText);
		editor_resp.setValue(result);

		$("#subResultButton").addClass('d-none');
	});
}

function pagingRESTQuery(contentType) {
	var queryName = $(":input:radio[name=queryName]:checked").val();
	if (queryName === 'events') {
		pagingRESTEventsQuery(contentType);
	} else if (queryName === 'vocabularies') {
		pagingRESTVocabulariesQuery(contentType);
	}
}

function pagingRESTEventsQuery(contentType) {
	var resourceType = $(":input:radio[name=resourceType]:checked").val();
	var resource = $(":input:radio[name=resource]:checked").val();

	if (resourceType === 'all') {
		queryURL = baseURL + "/events";
	} else if (resourceType == 'undefined') {
		alert('select a resource type');
		return;
	} else {
		if (resource == 'undefined') {
			alert('select a resource');
			return;
		}
		queryURL = baseURL + "/" + resourceType + "/" + encodeURIComponent(resource) + "/events";
	}

	let httpMethod;
	let httpBody;
	if (nextPageToken == null || nextPageToken === '') {
		queryURL = queryURL + "?perPage=20";
		httpMethod = "GET";
		httpBody = editor.getValue();
		httpBody = encodeURIComponent(httpBody);
	} else {
		queryURL = queryURL + "?perPage=20&nextPageToken=" + nextPageToken;
		httpMethod = "GET";
		httpBody = ""
	}

	$.ajax({
		type: httpMethod,
		url: queryURL,
		data: httpBody,
		headers: { 'GS1-EPCIS-Min': '2.0.0', 'GS1-CBV-Min': '2.0.0', 'GS1-EPCIS-Max': '2.0.0', 'GS1-CBV-Max': '2.0.0', 'GS1-EPC-Format': 'Always_GS1_Digital_Link', 'GS1-CBV-XML-Format': 'Always_Web_URI' },
		contentType: contentType,
		crossOrigin: true
	}).done((result, textStatus, request) => {

		let link = request.getResponseHeader('link');  // get 'Link' header
		if (link != null) {
			nextPageToken = link;
			$('#nextPage').removeClass('invisible');
			$('#hasNext').removeClass('invisible');
		}
		if (link == null) {
			nextPageToken = null;
			$('#nextPage').addClass('invisible');
			$('#hasNext').addClass('invisible');
		}

		// result formatting
		if (contentType.includes('json')) {
			editor_resp.setValue(JSON.stringify(result, null, 4));
			// 'fold' editor conetnts
			foldEventVoca(editor_resp, 'json');
		} else {
			let xmlString = formatXml(new XMLSerializer().serializeToString(result));
			// update editor contents
			editor_resp.setValue(xmlString);
			// 'fold' editor conetnts
			foldEventVoca(editor_resp, 'xml');
		}
	}).fail((result) => {
		// result formatting
		console.log(result);
		if (contentType.includes('json')) {
			editor_resp.setValue(JSON.stringify(result, null, 4));
		} else {
			let xmlString = formatXml(new XMLSerializer().serializeToString(result));
			// update editor contents
			editor_resp.setValue(xmlString);
		}
	});
}

function pagingRESTVocabulariesQuery(contentType) {
	var resourceType = $(":input:radio[name=resourceType]:checked").val();
	var resource = $(":input:radio[name=resource]:checked").val();

	if (resourceType === 'all') {
		queryURL = baseURL + "/vocabularies";
	} else if (resourceType == 'undefined') {
		alert('select a resource type');
		return;
	} else {
		if (resource == 'undefined') {
			alert('select a resource');
			return;
		}
		queryURL = baseURL + "/" + resourceType + "/" + encodeURIComponent(resource) + "/vocabularies";
	}

	let httpMethod;
	let httpBody;
	if (nextPageToken == null) {
		queryURL = queryURL + "?perPage=20";
		httpMethod = "GET";
		httpBody = editor.getValue();
		httpBody = encodeURIComponent(httpBody);
	} else {
		queryURL = queryURL + "?perPage=20&nextPageToken=" + nextPageToken;
		httpMethod = "GET";
		httpBody = ""
	}
	$.ajax({
		type: httpMethod,
		url: queryURL,
		data: httpBody,
		headers: { 'GS1-EPCIS-Min': '2.0.0', 'GS1-CBV-Min': '2.0.0', 'GS1-EPCIS-Max': '2.0.0', 'GS1-CBV-Max': '2.0.0', 'GS1-EPC-Format': 'Always_GS1_Digital_Link', 'GS1-CBV-XML-Format': 'Always_Web_URI' },
		contentType: contentType,
		crossOrigin: true
	}).done((result, textStatus, request) => {
		let link = request.getResponseHeader('Link');  // get 'Link' header
		if (link != null) {
			nextPageToken = link;
			$('#nextPage').removeClass('invisible');
			$('#hasNext').removeClass('invisible');
		}
		if (link == null) {
			nextPageToken = null;
			$('#nextPage').addClass('invisible');
			$('#hasNext').addClass('invisible');
		}

		// result formatting
		if (contentType.includes('json')) {
			editor_resp.setValue(JSON.stringify(result, null, 4));
			// 'fold' editor conetnts
			foldEventVoca(editor_resp, 'json');
		} else {
			let xmlString = formatXml(new XMLSerializer().serializeToString(result));
			// update editor contents
			editor_resp.setValue(xmlString);
			// 'fold' editor conetnts
			foldEventVoca(editor_resp, 'xml');
		}

	}).fail((result) => {
		// result formatting
		if (contentType.includes('json')) {
			editor_resp.setValue(JSON.stringify(result, null, 4));
		} else {
			let xmlString = formatXml(new XMLSerializer().serializeToString(result));
			// update editor contents
			editor_resp.setValue(xmlString);
		}
	});
}

// get capture job list
function getXMLCaptureJobList() {
	let captureJobListURL;
	if (nextPageToken == null) {
		captureJobListURL = baseURL + "/capture?perPage=10";
	} else {
		captureJobListURL = baseURL + "/capture?perPage=10&nextPageToken=" + nextPageToken;
	}

	$.ajax({
		type: "GET",
		url: captureJobListURL,
		contentType: "application/xml; charset=utf-8",
		crossOrigin: true,
		beforeSend: function(xhr) {  // add required headers
			xhr.setRequestHeader("GS1-EPCIS-Min", "2.0.0");
			xhr.setRequestHeader("GS1-EPCIS-Max", "2.0.0");
		}
	}).done((data, textStatus, request) => {
		$("#idList").empty();

		let link = request.getResponseHeader('Link');  // get 'Link' header

		if (link != null) {
			nextPageToken = link;
			$('#nextPage').removeClass('invisible');
			$('#hasNext').removeClass('invisible');
		}
		if (link == null) {
			nextPageToken = null;
			$('#nextPage').addClass('invisible');
			$('#hasNext').addClass('invisible');
		}


		captureJobList = data.getElementsByTagName("EPCISCaptureJob");

		for (let captureJob of captureJobList) {
			let cid = captureJob.getAttribute('captureID');
			let time;
			for (let innerCaptureJob of captureJob.childNodes) {
				if (innerCaptureJob.tagName == 'createdAt') {
					time = innerCaptureJob.textContent;
				}
			}
			newButton = $("<button type='button' class='list-group-item list-group-item-action'>" + time + "&ensp;|&ensp;" + cid + "</button>");
			newButton.on('click', function() {
				for (let idElem of $("#idList").children()) {
					idElem.classList.remove('active');
				}
				$(this).addClass('active');

				$.ajax({
					url: baseURL + "/capture/" + cid,
					contentType: "application/xml; charset=utf-8",
					dataType: "xml",
					crossOrigin: true,
					beforeSend: function(xhr) {
						xhr.setRequestHeader("GS1-EPCIS-Min", "2.0.0");
						xhr.setRequestHeader("GS1-EPCIS-Max", "2.0.0");
					}
				}).done((result) => {
					let xmlString = formatXml(new XMLSerializer().serializeToString(result));
					editor_resp.setValue(xmlString);
				});
			});
			$("#idList").append(newButton);
		}



	}).fail((result) => {
		console.log(result);
	});
}

function getJSONCaptureJobList() {
	let captureJobListURL;
	if (nextPageToken == null) {
		captureJobListURL = baseURL + "/capture?perPage=10";
	} else {
		captureJobListURL = baseURL + "/capture?perPage=10&nextPageToken=" + nextPageToken;
	}


	$.ajax({
		type: "GET",
		url: captureJobListURL,
		contentType: "application/json; charset=utf-8",
		crossOrigin: true,
		beforeSend: function(xhr) {  // add required headers
			xhr.setRequestHeader("GS1-EPCIS-Min", "2.0.0");
			xhr.setRequestHeader("GS1-EPCIS-Max", "2.0.0");
		}
	}).done((data, textStatus, request) => {
		$("#idList").empty();

		let link = request.getResponseHeader('Link');  // get 'Link' header

		if (link != null) {
			nextPageToken = link;
			$('#nextPage').removeClass('invisible');
			$('#hasNext').removeClass('invisible');
		}
		if (link == null) {
			nextPageToken = null;
			$('#nextPage').addClass('invisible');
			$('#hasNext').addClass('invisible');
		}

		for (let captureJob of data) {
			let cid = captureJob.captureID;
			let time = captureJob.createdAt;
			newButton = $("<button type='button' class='list-group-item list-group-item-action'>" + time + "&ensp;|&ensp;" + cid + "</button>");
			newButton.on('click', function() {
				for (let idElem of $("#idList").children()) {
					idElem.classList.remove('active');
				}
				$(this).addClass('active');

				$.ajax({
					url: baseURL + "/capture/" + cid,
					contentType: "application/json; charset=utf-8",
					dataType: "json",
					crossOrigin: true,
					beforeSend: function(xhr) {
						xhr.setRequestHeader("GS1-EPCIS-Min", "2.0.0");
						xhr.setRequestHeader("GS1-EPCIS-Max", "2.0.0");
					}
				}).done((result) => {
					editor_resp.setValue(JSON.stringify(result, null, 4));
				});
			});
			$("#idList").append(newButton);
		}



	}).fail((result) => {
		console.log(result);
	});
}


// Capture
function capture(format) {
	// capture (POST request)
	$.ajax({
		type: "POST",
		url: captureURL,
		data: editor.getValue(),
		contentType: "application/" + format + "; charset=utf-8",
		crossOrigin: true,
		beforeSend: function(xhr) {  // add required headers
			xhr.setRequestHeader("GS1-EPCIS-Version", "2.0.0");
			xhr.setRequestHeader("GS1-CBV-Version", "2.0.0");
			xhr.setRequestHeader("GS1-EPCIS-Capture-Error-Behaviour", "rollback");
		}
	}).done((data, textStatus, request) => {

		let loc = request.getResponseHeader('Location')  // get 'Location' header
		if (!loc) {
			// master data capture success
			$("#resp").val("[" + request.status + " " + request.statusText + "]").hide().fadeIn('slow');
			$("#status").removeClass("text-warning Blink").addClass("text-success");
			return;
		}
		let id = loc.split("/").pop();  // get capture ID from 'Location' header
		$("#resp").val("[" + request.status + " " + request.statusText + "] txID: " + id).hide().fadeIn('slow');
		$("#status").removeClass("text-secondary text-success text-danger").addClass("text-warning Blink");

		let cmp;  // for comparing the response with the previous response

		setTimeout(() => {
			let interval = setInterval(() => {
				// capture detail (GET request)
				if (format == "xml") {
					$.ajax({
						url: captureURL + "/" + id,
						contentType: "application/" + format + "; charset=utf-8",
						dataType: "xml",
						crossOrigin: true,
						beforeSend: function(xhr) {
							xhr.setRequestHeader("GS1-EPCIS-Min", "2.0.0");
							xhr.setRequestHeader("GS1-EPCIS-Max", "2.0.0");
						}
					}).done((result) => {
						let xmlString = formatXml(new XMLSerializer().serializeToString(result));
						let running = $(result).find("ns2\\:epcisCaptureJobType").attr("running");

						if (xmlString === cmp)  // 같은 응답 내용일 경우, 모달창 업데이트 및 성공 여부 검사 건너뜀
							return;

						cmp = xmlString;
						// update editor contents
						editor_details.setValue(xmlString);
						$('#detailsModal').on('shown.bs.modal', function() {
							editor_details.refresh();
						});

						if (running === "false") {
							let success = $(result).find("ns2\\:epcisCaptureJobType").attr("success");
							if (success === "true")
								$("#status").removeClass("text-warning Blink").addClass("text-success");
							else
								$("#status").removeClass("text-warning Blink").addClass("text-danger");

							clearInterval(interval);  // stop 'setInterval'
						}
					}).fail((result) => {
						$("#status").removeClass("text-warning Blink").addClass("text-danger");
						clearInterval(interval);
					});
				} else {

					$.ajax({
						url: captureURL + "/" + id,
						contentType: "application/" + format + "; charset=utf-8",
						dataType: "json",
						crossOrigin: true,
						beforeSend: function(xhr) {
							xhr.setRequestHeader("GS1-EPCIS-Min", "2.0.0");
							xhr.setRequestHeader("GS1-EPCIS-Max", "2.0.0");
						}
					}).done((result) => {
						let running = result.running;
						if (result === cmp)  // 같은 응답 내용일 경우, 모달창 업데이트 및 성공 여부 검사 건너뜀
							return;

						cmp = result;
						// update editor contents
						editor_details.setValue(JSON.stringify(result, null, 4));
						$('#detailsModal').on('shown.bs.modal', function() {
							editor_details.refresh();
						});

						if (running === false) {
							let success = result.success;
							if (success === true)
								$("#status").removeClass("text-warning Blink").addClass("text-success");
							else
								$("#status").removeClass("text-warning Blink").addClass("text-danger");

							clearInterval(interval);  // stop 'setInterval'
						}
					}).fail((result) => {
						$("#status").removeClass("text-warning Blink").addClass("text-danger");
						clearInterval(interval);
					});
				}
			}, 1000);
		}, 500);


	}).fail((result) => {
		if (format == 'xml') {
			var resultXML = formatXml(result.responseText);
			$("#resp").val("[" + result.status + " " + result.statusText + "] click ! button").hide().fadeIn('slow');
			// $("#status").removeClass("text-success text-warning text-danger").addClass("text-secondary");
			$("#status").removeClass("text-warning Blink").addClass("text-danger");
			// update editor contents
			editor_details.setValue(resultXML);
			$('#detailsModal').on('shown.bs.modal', function() {
				editor_details.refresh();
			});
		} else {
			var resultJSON = result.responseJSON;
			$("#resp").val("[" + result.status + " " + result.statusText + "] click ! button").hide().fadeIn('slow');
			// $("#status").removeClass("text-success text-warning text-danger").addClass("text-secondary");
			$("#status").removeClass("text-warning Blink").addClass("text-danger");

			// update editor contents
			editor_details.setValue(JSON.stringify(resultJSON, null, 4));
			$('#detailsModal').on('shown.bs.modal', function() {
				editor_details.refresh();
			});
		}
	});
}


// Single Capture 
function singleCapture(format) {
	// single capture (POST request)
	$.ajax({
		type: "POST",
		url: captureURL,
		data: editor.getValue(),
		contentType: "application/" + format + "; charset=utf-8",
		crossOrigin: true,
		beforeSend: function(xhr) {  // add required headers
			xhr.setRequestHeader("GS1-EPCIS-Version", "2.0.0");
			xhr.setRequestHeader("GS1-CBV-Version", "2.0.0");
		}
	}).done((data, textStatus, request) => {
		let loc = request.getResponseHeader('Location')  // get 'Location' header
		if (loc != null) {

			$("#resp").val("[" + request.status + " " + request.statusText + "] eventID: " + loc).hide().fadeIn('slow');
		} else {
			$("#resp").val("[" + request.status + " " + request.statusText + "]").hide().fadeIn('slow');
		}
		$("#status").removeClass("text-warning Blink").addClass("text-success");
		editor_details.setValue("");
	}).fail((result) => {

		if (result.hasOwnProperty('responseJSON') == false) {
			var resultXML = formatXml(result.responseText);
			editor_details.setValue(resultXML);
		} else {
			editor_details.setValue(JSON.stringify(result.responseJSON, null, 4));
		}

		$("#resp").val("[" + result.status + " " + result.statusText + "] click ! button").hide().fadeIn('slow');
		// $("#status").removeClass("text-success text-warning text-danger").addClass("text-secondary");
		$("#status").removeClass("text-warning Blink").addClass("text-danger")
		// update editor contents

		$('#detailsModal').on('shown.bs.modal', function() {
			editor_details.refresh();
		});
	});
}


// Query
function query() {
	// query (POST request)
	$.ajax({
		type: "POST",
		url: queryURL,
		data: editor.getValue(),
		contentType: "application/xml; charset=utf-8",
		crossOrigin: true
	}).done((result) => {
		// result formatting
		let xmlString = formatXml(new XMLSerializer().serializeToString(result));
		// update editor contents
		editor_resp.setValue(xmlString);
		// 'fold' editor conetnts
		foldEventVoca(editor_resp, 'xml');

		if ($(result).find("query\\:SubscribeResult").length) {
			const parser = new DOMParser();
			const xml = parser.parseFromString(editor.getValue(), 'application/xml');
			subId = $(xml).find('subscriptionID').text();  // in soapSubscribe.html

			$("#subResultButton").removeClass('d-none');
		} else {
			$("#subResultButton").addClass('d-none');
		}

	}).fail((result) => {
		// result formatting
		result = formatXml(result.responseText);
		editor_resp.setValue(result);

		$("#subResultButton").addClass('d-none');
	});
}

// Get subscription result from web client server
function getSubResult(subId) {
	const clientServerHref = window.location.href;
	let clientServerHrefArr = href.split("/");
	let webSocketUrl = "ws://" + hrefArr[2] + "/epcis/subSocket" + "?subId=" + subId;

	// clear editor contents
	editor_subResultResp.setValue("");
	$('#subResultModal').on('shown.bs.modal', function() {
		editor_subResultResp.refresh();
	});

	// open new websocket
	if (socket)
		socket.close();
	socket = new WebSocket(webSocketUrl);

	let responseToast = $("#subResponseToast")
	let responseToastHeader = responseToast.find("strong");
	let responseToastBody = responseToast.find(".toast-body");

	socket.onopen = function(event) {
		$("#subResultIcon").addClass("fa-spin");
		responseToastHeader.html("Connection established");
		responseToastBody.html("Please wait for the server to send the data");
		responseToast.toast("show");
	};

	socket.onmessage = function(event) {
		// only when modal is open
		if ($('#subResultModal').hasClass('show')) {
			// update editor contents
			editor_subResultResp.setValue(formatXml(event.data));

			responseToastHeader.html("Data received from server");
			responseToastBody.html("Please check the editor");
			responseToast.toast("show");
		}
	};

	socket.onclose = function(event) {
		$("#subResultIcon").removeClass("fa-spin");

		if (event.wasClean) {
			responseToastHeader.html("Connection closed cleanly.");
			responseToastBody.html(event.reason);
			responseToast.toast("show");
		} else {
			responseToastHeader.html("Connection closed abruptly.");
			responseToastBody.html("Connection died");
			responseToast.toast("show");
		}
	};

	socket.onerror = function(error) {
		$("#subResultIcon").removeClass("fa-spin");

		responseToastHeader.html("An error occured");
		responseToastBody.html("Please check the editor to see the error message");
		responseToast.toast("show");
		if (typeof event != `undefined`)
			editor_subResultResp.setValue(event.message);
		else
			editor_subResultResp.setValue("Browser probably cannot establish connection with " + url);
	};
}

// Retrieve examples
function retrieveExamples(retrieveSubURL, exampleMiddleURL, format) {
	let retrieveURL = baseURL.replace(":8080", "") + retrieveSubURL;  // from web client server
	$.ajax({
		url: retrieveURL,
		crossOrigin: true
	}).done((result) => {
		$.each(JSON.parse(result), (key, value) => {
			let tr = "<tr><td class=\"font-weight-bold h6\" style=\"width: 20%\">" + key + "</td><td style=\"width: 80%\">";

			$.each(value, (subIdx, subVal) => {
				if (subVal != "") {
					tr += "<button class=\"btn btn-outline-dark btn-sm mb-2\" id=\"/epcis/home" + exampleMiddleURL + "/" + key + "/" + subVal + "\" type=\"button\" onclick=\"loadExample(this, \'" + format + "\')\">" + subVal + "</button>"
				}
			})
			tr += "</td></tr>";

			$("#exampleBody").append(tr);
		})
	})
}

function retrieveExamples2(retrieveSubURL, exampleMiddleURL, format, target, target2) {
	let retrieveURL = baseURL.replace(":8080", "") + retrieveSubURL;  // from web client server
	$.ajax({
		url: retrieveURL,
		crossOrigin: true
	}).done((result) => {
		$.each(JSON.parse(result), (key, value) => {
			let tr = "<tr><td class=\"font-weight-bold h6\" style=\"width: 20%\">" + key + "</td><td style=\"width: 80%\">";

			$.each(value, (subIdx, subVal) => {
				if (subVal != "") {
					tr += "<button class=\"btn btn-outline-dark btn-sm mb-2\" id=\"/epcis/home" + exampleMiddleURL + "/" + key + "/" + subVal + "\" type=\"button\" onclick=\"loadExample2(this, \'" + format + "\',\'" + target2 + "\')\">" + subVal + "</button>"
				}
			})
			tr += "</td></tr>";

			$(target).append(tr);
		})
	})
}

// Load examples
function loadExample(btn, format) {
	$('#textArea').load(btn.id, () => {
		editor.setValue($('#textArea').val());  // update editor contents
		foldEventVoca(editor, format);  // 'fold' editor contents

		if (validationURL !== undefined)  // Whether to validate
			isValid(format);
	});

	$('#exampleModal').modal('hide');
}

function loadExample2(btn, format, target) {
	$('#textArea').load(btn.id, () => {
		editor.setValue($('#textArea').val());  // update editor contents
		foldEventVoca(editor, format);  // 'fold' editor contents

		if (validationURL !== undefined)  // Whether to validate
			isValid(format);
	});
	$(target).modal('hide');
}

// Initialize a new editor
function initEditor(id, format, readonly = false, size = 535) {
	let textAreaMode = format;
	if (format === "json")
		textAreaMode = "ld+json";

	let editor = CodeMirror.fromTextArea(document.getElementById(id), {
		matchBrackets: true,
		autoCloseBrackets: true,
		mode: "application/" + textAreaMode,
		lineNumbers: true,
		indentUnit: 4,
		theme: 'eclipse',
		foldGutter: true,
		gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
		readOnly: readonly
	});
	editor.setSize(null, size);
	return editor;
}

// Format xml code
function formatXml(xml) {
	var formatted = '';
	var reg = /(>)(<)(\/*)/g;
	xml = xml.replace(reg, '$1\r\n$2$3');
	var pad = 0;
	jQuery.each(xml.split('\r\n'), function(index, node) {
		var indent = 0;
		if (node.match(/.+<\/\w[^>]*>$/)) {
			indent = 0;
		} else if (node.match(/^<\/\w/)) {
			if (pad != 0) {
				pad -= 1;
			}
		} else if (node.match(/^<\w([^>]*[^\/])?>.*$/)) {
			indent = 1;
		} else {
			indent = 0;
		}

		var padding = '';
		for (var i = 0; i < pad; i++) {
			padding += '  ';
		}

		formatted += padding + node + '\r\n';
		pad += indent;
	});

	return formatted;
}

// Apply 'folding' to editor
function foldEventVoca(editor, format) {
	// xml
	if (format === 'xml') {
		let strs = editor.getValue().split("\n");
		const re_event = /<([a-zA-Z]+Event)>/;
		const re_voca = /<VocabularyElementList>/;

		// Fold events and vocabularies
		for (let i = 0; i < strs.length; i++) {
			if (strs[i].search(re_event) !== -1 || strs[i].search(re_voca) !== -1)
				editor.foldCode(CodeMirror.Pos(i, 0));
		}
	}
	// json
	else if (format === 'json') {
		let strs = editor.getValue().split("\n");

		// Fold events and vocabularies
		for (let i = 0; i < strs.length; i++) {
			if (strs[i].search("eventList") !== -1 || strs[i].search("vocabularyElementList") !== -1)
				editor.foldCode(CodeMirror.Pos(i, 0));
		}
	}
	else { }
}