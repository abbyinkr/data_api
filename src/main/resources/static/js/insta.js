//let accessToken;
let shortAccessToken;
let longAccessToken;
let userId;
let pageToken;
let businessId;
let pageId;
let fbName;
let status;



function statusChangeCallback(response) {  // Called with the results from FB.getLoginStatus().
	console.log('statusChangeCallback');
	console.log(response);                   // The current login status of the person.
	if (response.status === 'connected') {   // Logged into your webpage and Facebook.
		testAPI();
		shortAccessToken = response['authResponse'].accessToken;
		userId = response['authResponse'].userID;
		//console.log("단기 사용자 토큰 : " + shortAccessToken);
		console.log("userId : " + userId);
		document.getElementById('shortUserTkn').value = shortAccessToken;

	} else {                                 // Not logged into your webpage or we are unable to tell.
		document.getElementById('status').innerHTML = 'Please log ' +
			'into this webpage.';
	}
}


function checkLoginState() {               // Called when a person is finished with the Login Button.
	FB.getLoginStatus(function(response) {   // See the onlogin handler
		statusChangeCallback(response);
	});
}


window.fbAsyncInit = function() {
	FB.init({
		appId: '355495089884750',
		cookie: true,                     // Enable cookies to allow the server to access the session.
		xfbml: true,                     // Parse social plugins on this webpage.
		version: 'v13.0'           // Use this Graph API version for this call.
	});


	FB.getLoginStatus(function(response) {   // Called after the JS SDK has been initialized.
		statusChangeCallback(response);        // Returns the login status.
	});
};

function testAPI() {                      // Testing Graph API after login.  See statusChangeCallback() for when this call is made.
	console.log('Welcome!  Fetching your information.... ');
	FB.api('/me', function(response) {
		console.log('Successful login for: ' + response.name);
		document.getElementById('status').innerHTML =
			'Thanks for logging in, ' + response.name + '!';

	});
}

// 연동된 계정정보 저장
function addInfo() {

	FB.getLoginStatus(function(response) {
		statusChangeCallback(response)
		if (response.status === 'connected') {
			status = 1;
		} else {
			status = 9;
			return false;
		};
	});
	// 장기사용자토큰 발급
	FB.api(
		'/oauth/access_token',
		'GET',
		{
			"grant_type": "fb_exchange_token", "client_id": "355495089884750", "client_secret": "01e670535cdddc94c051ec9718feedd6"
			, "fb_exchange_token": shortAccessToken
		},
		function(response) {
			//console.log(response);
			longAccessToken = response.access_token;
			document.getElementById('longUserTkn').value = longAccessToken;
		
			// 폼데이터 생성
			let formdata = new FormData();
		
			formdata.append('status', status);
			formdata.append('longAccessToken', longAccessToken);
			formdata.append('userId', userId);
			
			$.ajax({
				url: '/insta/linkedaccount',
				type: 'POST',
				data: formdata,
				processData: false,
				contentType: false,
				success: function onData(data) {
					console.log(data);
					alert(data);
		
					// form 리셋
					formdata.forEach(function(val, key, fD) {
						formdata.delete(key)
					});
		
				},
				error: function onError(error) {
					console.log(error);
					alert(data);
				},
			});
			
		});

}

function logout() {

	FB.logout(function(response) {

		document.getElementById('status').innerHTML =
			'로그아웃되었습니다';
	});
}

//-- 수집데이터 
// account
function account() {
	
	$.ajax({
	url: '/insta/account',
	type: 'GET',
	success: function onData(data) {
		console.log(data);

	},
	error: function onError(error) {
		console.log(error);
	},
});
	
}

function media() {
	
	$.ajax({
	url: '/insta/media',
	type: 'GET',
	success: function onData(data) {
		console.log(data);

	},
	error: function onError(error) {
		console.log(error);
	},
});

	
}

function comment() {
	
	$.ajax({
	url: '/insta/comment',
	type: 'GET',
	success: function onData(data) {
		console.log(data);

	},
	error: function onError(error) {
		console.log(error);
	},
});

	
}





