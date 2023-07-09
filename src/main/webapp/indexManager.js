(function() {
	
	document.getElementById("loginButton").addEventListener("click", () => {
		var form = document.getElementById("loginForm");
		if(form.checkValidity()){
			makeCall("POST", "CheckLogin", form, 
			function(request) {
				var message = request.responseText;
				switch (request.status){
					case 200: //Login successful
						sessionStorage.setItem("username", message);
						window.location.href = "Home.html";
						break;
					default: //Login Unsuccessful
						document.getElementById("loginErrorMsg").textContent = message;
						break;
				}
			})
		}
		else{
			form.reportValidity();
		}
	})	
	
	document.getElementById("registrationButton").addEventListener("click", () => {
		var form = document.getElementById("registrationForm");
		if(form.checkValidity()){
			
			var username = form.elements.username.value;
			var name = form.elements.name.value;
			var surname = form.elements.surname.value;
			var email = form.elements.email.value;
			
			var regex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}$/;
			
			if(regex.test(email)) {
				if(form.elements.password.value === form.elements.re_password.value){
					
					makeCall("POST", "Registration", form, 
					function(request) {
						var message = request.responseText;
						switch (request.status){
							case 200: //Registration successful
								document.getElementById("registrationMessage").textContent = message;
								document.getElementById("registrationMessage").style.color = "green";
								break;
							default: //Registration Unsuccessful
								showErrorAndFillForm(message);
								break;
						}
					});
				}
				else{
					form.reset();
					showErrorAndFillForm("The two passwords don't match (lato client)");
				}
			}
			else{
				form.reset();
				showErrorAndFillForm("Invalid email (lato client)");
			}
		}
		else{
			form.reportValidity();
		}
		
		function showErrorAndFillForm(message){
			document.getElementById("registrationMessage").textContent = message;
			document.getElementById("registrationMessage").style.color = "red";
			form.elements.username.value = username;
			form.elements.name.value = name;
			form.elements.surname.value = surname;
			form.elements.email.value = email;
		}
		
	})
	
})();