

//--유튜브 수집데이터 
// channel
function channels() {

	
	$.ajax({
		url: '/youtube/channels',
		type: 'GET',
		success: function onData(data) {
			console.log(data);

		},
		error: function onError(error) {
			console.log(error);
		},
	});

}

function playlistItems() {

	$.ajax({
		url: '/youtube/playlistitems',
		type: 'GET',
		success: function onData(data) {
			console.log(data);

		},
		error: function onError(error) {
			console.log(error);
		},
	});


}

function videos() {

	$.ajax({
		url: '/youtube/videos',
		type: 'GET',
		success: function onData(data) {
			console.log(data);

		},
		error: function onError(error) {
			console.log(error);
		},
	});
}

function comments() {

	$.ajax({
		url: '/youtube/comments',
		type: 'GET',
		success: function onData(data) {
			console.log(data);

		},
		error: function onError(error) {
			console.log(error);
		},
	});


}





