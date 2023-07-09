{
	function checkLoggedUser(){ 
		
		if(sessionStorage.getItem("username")==null){
			window.location.href = "index.html";
		}
		else {
			document.getElementById("welcomeMessage").textContent="Nice to see you again, " + sessionStorage.getItem("username");
		}
		
	}
	

	function fillTables(){
		
		document.getElementById("createdMeetingsTableBody").innerHTML="";
		document.getElementById("invitedMeetingsTableBody").innerHTML="";

		makeCall("GET", "GetMeetings", null, 
		function(request){	
			if(request.getResponseHeader("loggedIn")==="false"){
					logout();
				}
			else{		
				switch(request.status){
					case 200:					
						var meetings = JSON.parse(request.responseText);
						var createdMeetings = meetings[0];
						var invitedMeetings = meetings[1];
						if(createdMeetings.length>0){
							var tableBody = document.getElementById("createdMeetingsTableBody");
							for(var i=0;i<createdMeetings.length;i++){
								var meeting = createdMeetings[i];
								var row = tableBody.insertRow();
								var titleCell = row.insertCell();
								titleCell.textContent = meeting.title;
								var dateTimeCell = row.insertCell();
								dateTimeCell.textContent = meeting.dateTime;
								var durationCell = row.insertCell();
								durationCell.textContent = meeting.duration;
							}
						}
						else{
							document.getElementById("createdMeetings").style.display= "none";
							document.getElementById("noCreatedMeetingsMessage").style.display="block";
						}
						if(invitedMeetings.length>0){
							var tableBody = document.getElementById("invitedMeetingsTableBody");
							
							for(var i=0;i<invitedMeetings.length;i++){
								var meeting = invitedMeetings[i];
								var row = tableBody.insertRow();
								var titleCell = row.insertCell();
								titleCell.textContent = meeting.title;
								var dateTimeCell = row.insertCell();
								dateTimeCell.textContent = meeting.dateTime;
								var durationCell = row.insertCell();
								durationCell.textContent = meeting.duration;
							}
						}
						else{
							document.getElementById("invitedMeetings").style.display= "none";
							document.getElementById("noInvitedMeetingsMessage").style.display="block";
						}
						break;
					default:
						document.getElementById("tablesErrorMessage").textContent=message;
						break;
				}
			}
			
		});
	}
	
	function logout(){
		makeCall("GET", "Logout", null, function(){
			sessionStorage.removeItem("username");
		window.location.href = "index.html";
		});
	}
	
	function anagraphicHandler(max){
		var tries=1;
		document.getElementById("anagraphicErrorMessage").textContent="";
		document.getElementById("maxInvitationsMessage").textContent="You can invite up to "+(max-1)+" people";
		document.getElementById("triesMessage").textContent="This is your try number "+tries+". You have up to 3 tries";
		showAnagraphic();
		
		var cancelButtonFunction = ()=>{
			document.getElementById("anagraphic").style.display="none";
			document.body.style.pointerEvents="all";
			document.getElementById("inviteButton").removeEventListener("click", inviteButtonFunction);
			document.getElementById("cancelButton").removeEventListener("click", cancelButtonFunction);
		}
		document.getElementById("cancelButton").addEventListener("click", cancelButtonFunction);
		
		var inviteButtonFunction = (e)=>{
			e.preventDefault();
			var checkboxes = document.getElementById("anagraphicTableBody").getElementsByTagName("input");
			var selectedUsers = [];
			for(var checkbox of checkboxes){
				if(checkbox.checked){
					selectedUsers.push(checkbox.value);
				}
			}
			if (selectedUsers.length>=max){
				tries++;
				if(tries>3){
					cancelButtonFunction();
					document.getElementById("meetingFormMessage").style.color = "red";
					document.getElementById("meetingFormMessage").textContent="Too many bad attempts, the meeting wasn't created";	
				}
				else{
					document.getElementById("triesMessage").textContent="This is your try number "+tries+". You have up to 3 tries";
					document.getElementById("anagraphicErrorMessage").textContent="You have selected too many users, please deselect at least "+(selectedUsers.length-max+1)+" users";
				}
			}
			else{
				var formData = new FormData(document.getElementById("meetingForm"));
				formData.append("invitedUsers", JSON.stringify(selectedUsers));
				makeCall("POST", "CreateMeeting", formData, 
				function(request){
					if(request.getResponseHeader("loggedIn")==="false"){
						logout();
					}
					else{		
						var message = request.responseText;
						switch (request.status){
							case 200: //Registration successful
								document.getElementById("meetingFormMessage").textContent = "Meeting created successfully";
								document.getElementById("meetingFormMessage").style.color = "green";
								fillTables();
								document.getElementById("meetingForm").reset();
								//setTimeout(fillTables, 2000);
								break;
							default: //Registration Unsuccessful
								document.getElementById("meetingFormMessage").textContent = message;
								document.getElementById("meetingFormMessage").style.color = "red";
								break;
						}
					cancelButtonFunction();
					}
				});
			}
		}
		document.getElementById("inviteButton").addEventListener("click", inviteButtonFunction);
	}
	
	function showAnagraphic(){
		
		makeCall("GET", "GetAnagraphic", null, 
		function(request){
			if(request.getResponseHeader("loggedIn")==="false"){
					logout();
				}
			else{
				switch(request.status){
					case 200:			
						var users = JSON.parse(request.responseText);
						var tableBody = document.getElementById("anagraphicTableBody");
						tableBody.innerHTML = "";
						for(var i=0;i<users.length;i++){
							var user = users[i];
							var row = tableBody.insertRow();
							var usernameCell = row.insertCell();
							usernameCell.textContent = user.username;
							var nameCell = row.insertCell();
							nameCell.textContent = user.name;
							var surnameCell = row.insertCell();
							surnameCell.textContent = user.surname;
							var checkboxCell = row.insertCell();
							var checkbox=document.createElement("input");
							checkbox.type="checkbox";
							checkbox.value=user.id;
							checkboxCell.appendChild(checkbox);
						}	
						break;
					default:
						document.getElementById("anagraphicServerErrorMessage").textContent=request.responseText;								
				}
			}
		});
		document.getElementById("anagraphic").style.display="block";
		document.body.style.pointerEvents = "none";
	}
	
	window.addEventListener("load", () => {
		var now=new Date();
		document.getElementById("dateTime").min=new Date(now.getTime()-now.getTimezoneOffset()*60000).toISOString().slice(0, -8);
		checkLoggedUser();
		fillTables();		
	});
	
	document.getElementById("logoutButton").addEventListener("click", logout);
	document.getElementById("meetingFormButton").addEventListener("click", ()=>{
		var form = document.getElementById("meetingForm");
		if(form.checkValidity()){			
			var max=form.elements.max.value;
			anagraphicHandler(max);			
		}
		else{
			form.reportValidity();
		}		
	});
	
}