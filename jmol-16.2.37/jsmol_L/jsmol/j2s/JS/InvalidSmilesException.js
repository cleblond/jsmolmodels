Clazz.declarePackage("JS");
Clazz.load(["java.lang.Exception"], "JS.InvalidSmilesException", null, function(){
var c$ = Clazz.declareType(JS, "InvalidSmilesException", Exception);
Clazz.makeConstructor(c$, 
function(message){
Clazz.superConstructor(this, JS.InvalidSmilesException, [message]);
JS.InvalidSmilesException.lastError = (message.startsWith("Jmol SMILES") ? message : "Jmol SMILES Exception: " + message);
}, "~S");
c$.getLastError = Clazz.defineMethod(c$, "getLastError", 
function(){
return JS.InvalidSmilesException.lastError;
});
c$.clear = Clazz.defineMethod(c$, "clear", 
function(){
JS.InvalidSmilesException.lastError = null;
});
Clazz.overrideMethod(c$, "getMessage", 
function(){
return JS.InvalidSmilesException.lastError;
});
c$.lastError = null;
});
;//5.0.1-v4 Wed Oct 09 10:23:43 CDT 2024