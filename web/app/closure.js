var MyClass = function(){
    this.secret = "hello world";    // non clousre pattern. Things are public.
    var secret2 = "top secret";     // clousre pattern. Effectively private
    this.printSec2 = function() {
            return secret2;
    };
    this.writeToClass = function() {
        $('.myTestClass').text(secret2);
    }
    this.methodToBeTested = function() {  
        $.ajax('/hello')
        .done(function (data) {
            if (typeof console == "object") {
                console.log("Success - " + data);
                $('.myTestClass').text(data);
            };
        })
        .fail(function (xhr, textStatus, error) {
            console.log(xhr.statusText);
            console.log(textStatus);
            console.log(error);
        });
}

};

MyClass.prototype.sayHi = function() {
    return this.secret;
}

var yo = new MyClass();
console.log(yo.secret + " " + yo.sayHi());
console.log(yo.secret2 + " " + yo.printSec2());

module.exports = MyClass;