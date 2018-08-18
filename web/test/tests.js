'use strict';

//require('qunit');
var MyClass = require('../app/closure.js');
var sinon = require('sinon');

QUnit.module('module 1', {
	before : function() {
    this.yo = new MyClass();
		this.server = sinon.fakeServer.create();
	},
	after : function() {
    this.server.restore();
  }
});

QUnit.test( "hello test", function( assert ) {
  assert.ok( 1 == "1", "Passed!" );
  assert.equal(1,1,"Passed!!!");
  assert.strictEqual(1,1,"Passed!!!");
  // assert.strictEqual(1,"1","Passed!!!");
});

QUnit.test( "closure test", function( assert ) {
  assert.equal(this.yo.printSec2(),"top secret","Passed!!!");
});

QUnit.test( "html test", function( assert ) {
  this.yo.writeToClass();
  assert.equal($(".myTestClass").text(),this.yo.printSec2(),"Passed!!!");
  // assert.dom('.myTestClass').hasText(yo.printSec2());
});

QUnit.test("should make an ajax call", function( assert ) {
  this.server.respondWith("GET", "/hello", [200, { "Content-Type": "application/json" }, JSON.stringify('hello world')]);
  this.yo.methodToBeTested();
  this.server.respond();
  assert.equal($(".myTestClass").text(),"hello world","Passed!!!");
  assert.ok(this.server.requests.length === 1, "One request was executed");
  assert.ok(this.server.responses.length === 1, "One response received");
});
