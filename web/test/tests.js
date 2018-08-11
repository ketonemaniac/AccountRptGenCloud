QUnit.module('module 1', {
	setup : function() {
		
	},
	teardown : function() {}
});

QUnit.test( "hello test", function( assert ) {
  assert.ok( 1 == "1", "Passed!" );
  assert.equal(1,1,"Passed!!!");
  assert.strictEqual(1,1,"Passed!!!");
  // assert.strictEqual(1,"1","Passed!!!");
});

QUnit.test( "closure test", function( assert ) {
  var yo = new MyClass();
  assert.equal(yo.printSec2(),"top secret","Passed!!!");
});