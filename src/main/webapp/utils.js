function makeCall(method, url, formElement, callback, reset=true) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function(){
		if (request.readyState===4){
			callback(request);
		}
	};
	request.open(method, url);
	if(formElement==null) {
		request.send();
	}
	else if(formElement instanceof FormData){
		 request.send(formElement);
	}
	else if(formElement instanceof HTMLFormElement) {
		request.send(new FormData(formElement));
	}
	if(formElement instanceof HTMLFormElement && formElement!== null && reset === true) {
		formElement.reset();
	}
}