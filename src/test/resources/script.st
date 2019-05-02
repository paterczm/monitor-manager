var assert = require('assert');

$http.get('<uri>',
  function (err, response, body) {
    assert.equal(response.statusCode, 200, 'Expected a 200 response!');
  }
);