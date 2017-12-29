var http = require('http');
var fs = require("fs");
 
console.log("testing client webpage");

http.createServer(function(request, response) {
	var endpoint = request.url.toString();
	if (endpoint == "/") endpoint = "index.html";
	console.log("filename is " + endpoint);

	if (endpoint == "/uploadFile") {
	  response.writeHead(200, {'Content-Type': 'application/json'});
	  response.write("{\"filename\":\"D5.docx\",\"password\":\"a12345678\", \"generationTime\":1513496077726, \"company\" : \"ABC Company\"}");
	  response.end();
		return;		
	}
	if (endpoint == "/listFiles") {
	  response.writeHead(200, {'Content-Type': 'application/json'});
	  response.write("[{\"company\":null,\"filename\":\"6886Sell-20171202112253.pdf\",\"password\":null,\"generationTime\":null},{\"company\":null,\"filename\":\"coverage-error-20171116115549.log\",\"password\":null,\"generationTime\":null}]");
	  response.end();
		return;		
	}
	var fullName = "src/main/resources/static/" + endpoint;
	if(!fs.existsSync(fullName)) {
		response.writeHead(404, {'Content-Type': 'text/plain'});
		response.end();
		return;
	}
	fs.readFile(fullName, function(err, data){
	  response.writeHead(200, {'Content-Type': 'text/html'});
	  response.write(data);
	  response.end();
	});
}).listen(3000);

process.on('uncaughtException', function (err) {
  console.log('Caught exception: ' + err);
});