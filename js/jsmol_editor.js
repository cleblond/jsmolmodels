
/*
delete Jmol._tracker;

//Jmol._isAsync = false;

                    // last update 2/18/2014 2:10:06 PM
var jmolApplet;
var jmolApplet0; // set up in HTML table, below

var EZcalced = false;

jmol_isReady = function(applet) {
	//document.title = (applet._id + " - Jmol " + Jmol.___JmolVersion)
	Jmol._getElement(applet, "appletdiv").style.border="1px solid gray";
	console.log("READDDY");
}		


//console.log(initial.value);


var Info = {
            width: 500,
            height: 500,
	debug: false,
	zIndexBase: 1000,
    deferApplet: false,
    deferUncover: false,
    //deferApplet: true,
    color: "0xFFFFFF",
    addSelectionOptions: true,
    use: "HTML5",   // JAVA HTML5 WEBGL are all options
    //j2sPath: "/jmol/jsmol/j2s", // this needs to point to where the j2s directory is.
    j2sPath: "jmol-16.2.37/jsmol/j2s", // this needs to point to where the j2s directory is.
    //jarPath: "/jmol/jsmol/java",// this needs to point to where the java directory is.
    //jarFile: "JmolAppletSigned.jar",
    //isSigned: true,
    script: "set zoomlarge false;set antialiasDisplay; frank OFF;  set StructureModifiedCallback 'StructureModifiedCallback'; set PickCallback 'PickCallback'; set LoadStructCallback 'LoadStructCallback'; set modelKitMode true; set showAtomTypes true",
    
    serverURL: "jmol-16.2.37/jsmol/php/jsmol.php",
    //readyFunction: jmol_isReady,
    disableJ2SLoadMonitor: true,
    disableInitialConsole: true,
    allowJavaScript: true
    //defaultModel: "$dopamine",
    //console: "none", // default will be jmolApplet0_infodiv, but you can designate another div here or "none"
}

jmolApplet0 = Jmol.getApplet("jmolApplet0", Info);
*/


delete Jmol._tracker;

//Jmol._isAsync = false;

                    // last update 2/18/2014 2:10:06 PM
var jmolApplet;
var jmolApplet0; // set up in HTML table, below

var EZcalced = false;

jmol_isReady = function(applet) {
    //document.title = (applet._id + " - Jmol " + Jmol.___JmolVersion)
    Jmol._getElement(applet, "appletdiv").style.border="1px solid gray";
    console.log("READDDY");
}		


console.log(showsearch);


var Info = {
    width: 500,
    height: 500,
    debug: false,
    zIndexBase: 1000,
    deferApplet: false,
    deferUncover: false,
    //deferApplet: true,
    color: "0xFFFFFF",
    addSelectionOptions: true,
    use: "HTML5",   // JAVA HTML5 WEBGL are all options
    //j2sPath: "/jmol/jsmol/j2s", // this needs to point to where the j2s directory is.
    j2sPath: "jmol-16.2.37/jsmol/j2s", // this needs to point to where the j2s directory is.
    //jarPath: "/jmol/jsmol/java",// this needs to point to where the java directory is.
    //jarFile: "JmolAppletSigned.jar",
    //isSigned: true,
    script: "set zoomlarge false; set antialiasDisplay; frank OFF;  set StructureModifiedCallback 'StructureModifiedCallback'; set PickCallback 'PickCallback'; set LoadStructCallback 'LoadStructCallback'; set elementkey On;  //set modelKitMode true; //set showAtomTypes true",
    
    serverURL: "jmol-16.2.37/jsmol/php/jsmol.php",
    //readyFunction: jmol_isReady,
    disableJ2SLoadMonitor: true,
    disableInitialConsole: true,
    allowJavaScript: true,
    addSelectionOptions: showsearch
    //defaultModel: "$dopamine",
    //console: "none", // default will be jmolApplet0_infodiv, but you can designate another div here or "none"
}

jmolApplet0 = Jmol.getApplet("jmolApplet0", Info);



var modelEdit = true;
var lastPrompt=0;
var undos = ["", "", "", "", "" ,"", "", "", "", ""];
var redos = ["", "", "", "", "" ,"", "", "", "", ""];
var stereoToggle = 1;
var modelBkg1 = "#0000ff";
var fileType = "mol";



function jmv(scpt) { return Jmol.evaluateVar(jmolApplet0, scpt); }
function exMod() { return jmstr("extractModel"); }
function jmstr(scpt) { return Jmol.getPropertyAsString(jmolApplet0,scpt); }


function trim(str) {
	if (str != null) { return str.replace(/^\s+|\s+$/g, ""); }
	return null;
}



function jmscript(scpt) { Jmol.script(jmolApplet0, scpt); }

function mkResetMin() {
        
	jmscript('unbind; unbind _wheelZoom; unbind "DOUBLE"; set picking off; set picking on; set allowRotateSelected false;');
	deleteModel = false; modelEdit = false; colorChange = false;
        
	//writeHL("off");
}

function hasCharge() {
	var pos = Math.abs(jmv("{*}.formalCharge.max"));
	var neg = Math.abs(jmv("{*}.formalCharge.min"));
	if (pos > 0 || neg > 0) {
		return true;
	}
	return false;
}  





function resetJsmol(){
        console.log("reset jsmol");

        Jmol.script(jmolApplet0,'set modelKitMode true; zap;' );

}  


function getEchoColor(hexcolor){
	if (stereoToggle == 2) { return "white"; }
	hexcolor = hexcolor.replace("#", "");
	var r = parseInt(hexcolor.substr(0,2),16);
	var g = parseInt(hexcolor.substr(2,2),16);
	var b = parseInt(hexcolor.substr(4,2),16);
	var yiq = ((r*299)+(g*587)+(b*114))/1000;
	//return (yiq >= 128) ? 'black' : 'white';
	return 'blue';
}      



function echo(msg, loc, delay, font, color) {
	msg = (!msg) ? "" : msg;
	font = (!font) ? "18" : font;
	color = (!color) ? getEchoColor(modelBkg1) : color;
	delay = (!delay) ? "" : delay;
	loc = (!loc) ? "top left" : loc;
	menuItem = 0; globalVar = "";
	//if (msg == 1) { msg = "Dbl-Click window to toggle p orbitals off/on... |" }
		//if (msg == 2) { msg = "Dbl-Click window to toggle p orbitals off/on... |All orbitals for this model are loaded automatically |" }
		//if (msg == "") { jmscript("set echo bottom left;echo ;set echo top left;echo ;"); return null;  }
		if (delay == "") {
			jmscript("mo off;set echo " + loc + ";font echo " + font + " serif;color echo  " + color + ";echo " + msg + " |;refresh;delay 0.1");
		}
		//else {
		//	jmscript("mo off;set echo " + loc + ";font echo " + font + " serif;color echo  " + color + ";echo " + msg + " |;refresh;delay " + delay + ";echo ");
		//}
		return null;
	}

	function typeCheck(typ) {
		var typAppend = "";
		if (typ == "ms" && (fileType == "mol" || fileType == "spartan")) {
			return true;
		}
		if (typ == "msp" && (fileType == "mol" || fileType == "spartan" || fileType == "pdb")) {
			return true;
		}
		if (typ == "ms1" && ckModNum() == 1 && (fileType == "mol" || fileType == "spartan")) {
			return true;
		}
		if (typ == "m1" && ckModNum() == 1 && fileType == "mol") {
			return true;
		}
		if (typ == "m" && fileType == "mol") {
			return true;
		}
		if (typ == "s" && fileType == "spartan") {
			return true;
		}
		if (typ == "c" && fileType == "cif") {
			return true;
		}
		if (typ == "p" && fileType == "pdb") {
			return true;
		}
		if (typ == "ms")  { typAppend = "molfiles and Spartan files."; }
		if (typ == "msp")  { typAppend = "molfiles, Spartan files, and pdb files."; }
		if (typ == "ms1")  { typAppend = "single molfiles and Spartan files."; }
		if (typ == "m1")  { typAppend = "single molfiles."; }
		if (typ == "m")  { typAppend = "molfiles."; }
		if (typ == "s")  { typAppend = "Spartan files."; }
		if (typ == "c")  { typAppend = "cif files."; }
		if (typ == "p")  { typAppend = "pdb files."; }

		return false;
	}

/*
	function promptAlt(t,v,c,d) {
	
	    //console.log(t,v,c,d);
		d = (!d)?"":d;
		keysOpen = true;
		bootbox.prompt({
			title: t,
			value: v,
			callback: function(result) {
				keysOpen = false;
				if (result === null) {
					bootbox.hideAll();
				} else {
					result = trim(result);
					if (c == "osrB") { bootbox.hideAll(); procBtn(d,result); }

				}
			}
		});
	}
*/
	


function showPT() {

			$('#ptModal').modal('show');

}



calcscript = ";select *;calculate chirality;set labelfor {*} \"%[chirality]\";"




function invertStereo() {

    console.log("Invert Stereo");
    echo("Click on a stereocenter to invert i.");
    jmscript("set picking invertstereo;");

}




function moveMol(num) {
	//if (!typeCheck("msp")) { return null; }
	mkResetMin();
	jmscript("set antialiasDisplay false");
	//console.log(num);
	if (num == 1) {
		echo("Click-Drag model to TRANSLATE model. |");
		jmscript('set picking off; set picking ON; set atomPicking true; set allowRotateSelected TRUE; set picking dragmolecule;bind "double" "javascript moveMol(2)";');
	}
	if (num == 2) {
		echo("Click-Drag model to ROTATE model. |");
		jmscript('set picking off; set picking ON;set atomPicking true;set picking dragmolecule;set allowRotateSelected TRUE;bind "drag" "_rotateselected";bind "double" "javascript moveMol(3)";');
	}
	if (num == 3) {
		echo("Click-Drag atom to DRAG atom. |");
		jmscript('set picking off; set picking ON; set picking DRAGATOM; bind "double" "javascript moveMol(1)";');
	}
	if (num == 4) {
		var ecStr = "Click a bond to select it. After selection, Click-Drag on circled atoms to rotate its branch.";
		echo(ecStr);
		console.log()
		//var scpt = 'set modelKitMode false; set Picking OFF;set Picking ON; set Picking ROTATEBOND; unbind "LEFT-DRAG";bind "LEFT-DRAG" "_rotateBranch"; hover off;unbind "WHEEL"; bind "WHEEL" "select *;color cpk"';
		//var scpt = 'set modelKitMode false;';
		var scpt = 'set modelKitMode false; hover ON;set Picking ROTATEBOND;';
		jmscript(scpt);
	}
}


function deleteAtomBond(num) {
	modelEdit = true;
	mkResetMin();
	
	//jmscript('set picking off; set picking on; set atomPicking true;set picking DELETEATOM; bind "double" "javascript deleteAtomBond(2)";');
	
	
	if (num == 1) {
		    echo("Click an atom to delete the atom.");
			jmscript('set picking off; set picking on; set atomPicking true;set picking DELETEATOM; bind "double" "javascript deleteAtomBond(2)";');
	}
	if (num == 2) {
		    echo("Click a bond to delete the bond.");
			scpt = 'set picking assignBond_0; bind "double" "javascript deleteAtomBond(1)";';
			jmscript(scpt);
			jmscript('hover off;');
	}
	
	
	
	
}





function procBtn(scpt,d) {
	d=(!d)?"":d;
	
	
	if (scpt == "rotateB") {
		if (!typeCheck("ms")) { return null; }
		mkResetMin();
		moveMol(4);
		return null;
	}
	
	
	if (scpt == "ring3") {
		mkResetMin();
		jmscript('load async "$cyclopropane";;n = ({molecule=1}.length < {molecule=2}.length ? 2 : 1); select molecule=n;display selected;center selected');
            
		return null;
	}
	
        if (scpt == "ring4") {
		mkResetMin();
		jmscript('load async "$cyclobutane";;n = ({molecule=1}.length < {molecule=2}.length ? 2 : 1); select molecule=n;display selected;center selected');
            
		return null;
	}
	
	if (scpt == "ring5") {
		mkResetMin();
		jmscript('load async "$cyclopentane";;n = ({molecule=1}.length < {molecule=2}.length ? 2 : 1); select molecule=n;display selected;center selected');
            
		return null;
	}
	
	if (scpt == "ring6") {
		mkResetMin();
		jmscript('load async "$cyclohexane";;n = ({molecule=1}.length < {molecule=2}.length ? 2 : 1); select molecule=n;display selected;center selected');
            
		return null;
	}
	
	if (scpt == "ringar6") {
		mkResetMin();
		jmscript('load async "$benzene";;n = ({molecule=1}.length < {molecule=2}.length ? 2 : 1); select molecule=n;display selected;center selected');
            
		return null;
	}
	
	
	
	if (scpt == "zoomI") {
		jmscript("zoom +20");
		return null;
	}
	if (scpt == "zoomO") {
		jmscript("zoom -20");
		return null;
	}
	if (scpt == "correctH") {
		mkResetMin();
		if (!typeCheck("m")) { return null; }
		jmscript('delete hydrogen;calculate hydrogens');
		echo("All H atoms have been deleted and recalculated. |Some H atoms may have to be added manually.");
		return null;
	}
	if (scpt == "optimizeM") { 
		mkResetMin();
		if (!typeCheck("m")) { return null; }
		if (hasCharge()) { echo("Optimization cannot be done on |molecules with formal charge."); return null; }
		echo("Minimization calculation in progress...");
		var atms = jmv("{*}.size");
		var stps = 50;
		if (atms > 15) { stps = 40; }
		if (atms > 30) { stps = 30; }
		if (atms > 50) { stps = 20; }
		scpt = "select xenon;select unselected;minimize steps " + stps + ";select *; wireframe 0.15; spacefill 23%; boundbox {*}; centerat boundbox; zoom 0; javascript echo(globalVar)";
		menuItem = 99;
		jmscript(scpt);
		return null;
	}


	if (scpt.substring(0, 4).toLowerCase() == "atom") {
		mkResetMin();
		modelEdit = true;
		//if (!typeCheck("m")) { return null; }
		var atom = scpt.replace(/atom/i,"").toString();
		if (atom == "0") {
			jmscript("set picking off; set picking on; set atomPicking true;set picking DELETEATOM");
			echo("Click an atom to delete the atom.");
			return null;
		}
		if (atom == "X") {
			atom = d;
		}
		scpt = "set picking off; set picking on; set atomPicking true;set picking assignatom_" + atom;
		jmscript(scpt);
		echo("Click an atom to replace it with " + atom + ". Also, click dragging |can be used to connect and add atoms.");
		return null;
	}

	if (scpt.substring(0, 4).toLowerCase() == "bond")  {
		mkResetMin();
		modelEdit = true;
		replacementBond = "";
		//if (!typeCheck("m")) { return null; }
		scpt = trim(scpt.replace(/bond/i, ""));
		if (scpt == "0" || scpt == "1" || scpt == "2"  || scpt == "3") {
			if (scpt == "1") { replacementBond = "single"; }
			if (scpt == "2") { replacementBond = "double"; }
			if (scpt == "3") { replacementBond = "triple"; }
			if (scpt == "0"){ replacementBond = "Click a bond to delete the bond"; }
			if (scpt != "0"){ replacementBond = "Click a bond to convert it to a " + replacementBond + " bond."; }
			echo(replacementBond);
			scpt = "set picking assignBond_" + scpt.toString();
			jmscript(scpt);
			jmscript("hover off");
		}
		return null;
	}
	
	
	
	if (scpt.substring(0, 4).toLowerCase() == "undo")  {
	    console.log("undo");
		mkResetMin();
		modelEdit = true;
		replacementBond = "";
		//if (!typeCheck("m")) { return null; }
		scpt = 'undo';
		jmscript(scpt);
		jmscript("hover off");
		//}
		return null;
	}
	
	return null;
	
	
	
}



function stashUndo(str) {
	str = (!str)?"":str;
	var x; var y;
	if (str != "") {
		x = undos.push(str);
	}
	else {
		x = undos.push(exMod());
	}
	y = undos.shift();

}




function calculateChiralityForAtom(atomIndex) {
        
            //console.log(atomIndex);
            
            jmscript(`
          
                select atomIndex=${atomIndex};
                label off;
                print atomIndex;
                calculate chirality;
                taVar1 = {selected}.chirality;
                print taVar1;
                //if (taVar1 == "") { taVar1 = "**" };
                if ({selected}.element=="H") { taVar1=ghpc() };
                if ({selected}.label == "R" || {selected}.label == "S" || {selected}.label == "*" || {selected}.label == "E" || {selected}.label == "Z" || {selected}.label == "Hs" || {selected}.label == "Hr") {
                    select selected or connected(double, selected);
                    label "*"; label off;
                } else {
                    select selected or connected(double, selected);
                    color labels black;
                    set fontsize 12;
                    background labels yellow;
                    
                    label @taVar1;
                };
                
            `);
        
}

        // Function to loop through all atoms and apply chirality calculation
        
/*
function calculateChiralityForAllAtoms() {

            console.log('calculateChiralityForAllAtoms');
            
            var totalAtoms = Jmol.evaluateVar(jmolApplet0, "{*}.size;");
            
            console.log("Total atoms: ", totalAtoms);

            for (var i = 0; i < totalAtoms; i++) {
                calculateChiralityForAtom(i); // Apply chirality calculation for each atom
            }
}
*/



function calculateChirality() {
    if (showstereo) {
      jmscript('select *;calculate chirality;set labelfor {*} \"%[chirality]\"; select *;font label 20;set labeloffset 10 10; background label yellow;color labels black;set labelfor {*} \"%[chirality]\";');
    }

}

/*
function calculateEZChirality() {
            console.log("Calculating E/Z Chirality for Double Bonds");

            
            
            //works
            jmscript(`
          
                select connected(double);
                label off;
                print atomIndex;
                calculate chirality;
                taVar1 = {selected}.chirality;
                print "here";
                print taVar1;
                print "here2";
                //if (taVar1 == "") { taVar1 = "**" };
                if ({selected}.element=="H") { taVar1=ghpc() };
                if ({selected}.label == "R" || {selected}.label == "S" || {selected}.label == "*" || {selected}.label == "E" || {selected}.label == "Z" || {selected}.label == "Hs" || {selected}.label == "Hr") {
                    select selected or connected(double, selected);
                    label "*"; label off;
                } else {
                    select selected ;
                    color label black;
                    set fontsize 10;
                    background labels yellow;
            
                    label @taVar1;
                };
                
            `);
             

        }


*/




function LoadStructCallback(a,b,c,d,e,f,g,h) {
    //console.log('LSCB');
    console.log("loadstructcallback modelkitmode="+modelEdit);
    
    console.log(a,b,c,f);
    console.log(''+f);
    
    //var totalAtoms2 = Jmol.scriptEcho(jmolApplet0, "print {*}.size");
    //console.log("total atoms2 LSCallBack="+totalAtoms2);
    //*var showstereo = 'select {*}; calculate chirality; select atomIndex=_atomPicked; taVar1 = {selected}.chirality; if (taVar1 == "") { taVar1 = "*" }; if ({selected}.element=="H") { taVar1=ghpc() }; if ({selected}.label == "R" || {selected}.label == "S" || {selected}.label == "*" || {selected}.label == "E" || {selected}.label == "Z" || {selected}.label == "Hs" || {selected}.label == "Hr") {select selected or connected(double, selected); label "*"; label off;} else { select selected or connected(double, selected); color label pink; set fontsize 10; background labels red; label @taVar1;}';
    
    if (f = 3) {
        calculateChirality();
    //calculateChiralityForAllAtoms();

        jmscript('set modelKitMode true; set showMenu false;');
    
    }
    //jmscript('set modelKitMode true;' + showstereo);
    //console.log(modelEdit);
    //console.log(spart1);
    
    
    
    
    if (modelEdit) { return null; }


	return null;
}


function StructureModifiedCallback(x, y, z) {
    console.log("structuremodifiedcallback");
    var xx = '' + x;
    //console.log('' + x, '' + y,'' + z);
    //if
    
        calculateChirality();
        //calculateChiralityForAllAtoms();

    stashUndo();

    if (y > 0) {
        console.log("y > 0");


    	
    	
    	//jmscript('select *; wireframe 0.15; spacefill 23%; boundbox {*};centerat boundbox; color label pink;set fontsize 12; label ""; select formalCharge <> 0;label %C;unbind; unbind "DOUBLE"; javascript stashMol(), set modelKitMode true;');
    }
}


function PickCallback(x, y, z) {
    console.log('PICKCB');
	console.log(x + "||||" + y + "||||" +  z);
	var scpt = ""; var at1 = ""; var at2 = "";
	if (y.indexOf("inverted") > -1) {
	
	    console.log("PCB inverted");
	    calculateChiralityForAllAtoms();
	    /*
		scpt= 'select _H; label ""; select atomIndex=' + z + ';var x = {selected}.chirality; if (x == "") { x = "*" } if ({selected}.label != "R" && {selected}.label != "S" && {selected}.label != "*" && {selected}.label != "E" && {selected}.label != "Z") { select selected or connected(double, selected); label "."; label off} else { select selected or connected(double, selected); color label pink; set fontsize 10; background labels red; label @x;}'; mkResetMin();echo();stashMol();jmscript(scpt);
	
	    */
	
	}
	if (deleteModel) {
		at1 = y.split("#")[1].split(" ")[0];
		scpt = 'select within(branch, {atomno=1000}, {atomno=' + at1 + '});delete selected;';
		jmscript(scpt);
	}
	if (colorChange) {
		scpt = 'select {*}; calculate chirality; select atomIndex=_atomPicked; taVar1 = {selected}.chirality; if (taVar1 == "") { taVar1 = "*" }; if ({selected}.element=="H") { taVar1=ghpc() }; if ({selected}.label == "R" || {selected}.label == "S" || {selected}.label == "*" || {selected}.label == "E" || {selected}.label == "Z" || {selected}.label == "Hs" || {selected}.label == "Hr") {select selected or connected(double, selected); label "*"; label off;} else { select selected or connected(double, selected); color label pink; set fontsize 10; background labels red; label @taVar1;}';
		jmscript(scpt);
	}
}


function stashMol() { // Set chemagicTEMP for model transfer data
	if (stereoToggle == 2 ) { jmscript("stereo " + modelstereo + ";background " + stereoBkg + ";select hydrogen or carbon; select not selected;color label white;label %e; select formalCharge <> 0; label %C");  if (fileType == "cif" || fileType == "pdb") { jmscript('select *;label ""'); } }
	if (fileType == "spartan" || fileType == "cif" || fileType == "pdb") { return null; }
	if (storageOn && fileType != "pdb") {
		localStorage.setItem("chemagicTEMP", "!MODS!" + exMod());
		localStorage.setItem("chemagicTEMPS", "!MODSM!" + getSmiles());
	}
	return null;
}

	function getUndo() {
	    console.log("getundo");
		if (fileType != "mol") { return null; }
		modelEdit = false;
		var x; var y; var z; var stash;
		stash = undos.pop();

		z = undos.unshift("");
		if (stash == null || stash == undefined || stash == "") { return false; }
		x = redos.push(exMod());
		y = redos.shift();
		var molf1 = stash;
		jmscript('var molf2 = "' + molf1 + '"; load "@molf2"; hover off;');
		return true;
	}    

	function getRedo() {
		if (fileType != "mol") { return null; }
		var x; var y; var z; var stash;
		stash = redos.pop();
		z = redos.unshift("");
		if (stash == null || stash == undefined || stash == "") { return false; }
		x = undos.push(exMod());
		y = undos.shift();
		var molf1 = stash;
		jmscript('var molf2 = "' + molf1 + '"; load "@molf2"; hover off;');
		return true;
	}    

/*
	function aClickActionP(num) {
		if (!typeCheck("ms") && num == 1) { return null; }
		if (fileType != "mol" && fileType != "spartan" && fileType != "cif") {
			//var t = "<div style='font-size:18px'><h4>CheMagic Model Kit: Confirm</h4><p style='text-align:left'>A loaded PDB file will be lost with this action. SPARTAN and CIF files will be retained, but they will be converted to molfiles. Your currently loaded model is a " + fileType.toUpperCase() + " file.</p></div>";
			var c = "aClickActionB";
		//confirmAlt(t,c,num); //Bootbox Confirm for non-molfile before aClickActionB
	} else { aClickActionB(num, true); } // Molfile to aClickActionB
	return null;
}


function aClickActionB(num, result) {
	num = parseInt(num);
	if (!result) { return null; }
	if (result && fileType != "pdb") { stashMol(); }
	if (result && storageOn) {
		localStorage["chemagicTEMP2"] = JSON.stringify(undos);
		localStorage["chemagicTEMP3"] = JSON.stringify(redos);
		if (num == 1) {
			stashFormula();
			if (isEmbedded()) { window.location.href = "acalculator.htm"; }
			else {window.location.href = "acalculator.htm?pid=on";}
		}
		if (num == 2) {
			//stashMol();
			if (isEmbedded()) {
				window.parent.iframeLoad(2);
				//window.location.href = "amodel.htm";
			}
			else {
				window.location.href = "amodel.htm";
			}
		}
		return null;
	}
	else {
		return null;
	}
	return null;
}          

*/
$( document ).ready(function() {



	//$("#jsmoldiv").html(Jmol.getAppletHtml("jmolApplet0", Info))






    $( "#jmolApplet0_submit" ).after( '<button onclick="Jmol.loadFileFromDialog(jmolApplet0)">Load File</button>' );


    function getFormData($form){
        var unindexed_array = $form.serializeArray();
        var indexed_array = {};

        $.map(unindexed_array, function(n, i){
            indexed_array[n['name']] = n['value'];
        });

        return indexed_array;
                   
    }


    $('#toggle-settings').click(function(e) {
      e.preventDefault();
      var settingsDiv = $('#settings');
      settingsDiv.toggle();
      
      // Update the button text based on the visibility of the settings div
      if (settingsDiv.is(':visible')) {
        $('#toggle-settings').text('Hide Settings');
      } else {
        $('#toggle-settings').text('Show Settings');
      }
    });



	var table = Kekule.Widget.getWidgetById('peridicTable');
	table.useMiniMode= true;
	table.enableMultiSelect = false;
	
	
    $( "td" ).on( "dblclick", function() {
        procBtn("atom"+table.selected.symbol)
        $('#ptModal').modal('hide');
        $('#atombtn span.K-Text-Content').html(table.selected.symbol);

    } );
			

    $("form").submit(function (event) {
        
        var formData = getFormData($('form'));       


        $.ajax({
          type: "POST",
          url: $('#ajax').val(),
          data: formData,
          dataType: "json",
          encode: true,
        }).done(function (data) {
        });

        event.preventDefault();
    });

    $( "#initialbtn" ).click(function(e) {
        e.preventDefault();
      
       //var molfile = Jmol.getPropertyAsString(jmolApplet0, "extractModel");
       
       var stateinfo = Jmol.getPropertyAsString(jmolApplet0, "stateInfo");
       var stateinfojson = Jmol.getPropertyAsString(jmolApplet0, "stateInfo");
       
       console.log(Jmol.getPropertyAsJSON(jmolApplet0, "stateInfo"));
 
       $("#initial").val(stateinfo);
      
      
    });
    
    $( "#viewinitialbtn" ).click(function(e) {
        e.preventDefault();

         //var initial = '"model example"'+$("#initial").val() +' end "model example"';
         
         //Jmol.script(jmolApplet0,'data '+initial+'; show data;' );
         //Jmol.script(jmolApplet0,'load '+$("#initial").val()+'; show data;' );
         

    });
    
    
    	setTimeout(function() {


	    if (!initial) {
		console.log("iniital not set");
		Jmol.script(jmolApplet0,'set modelKitMode true; zap;' );
	    	resetJsmol();
	    }




	}, 200);
	
	
	if (initial) {
	    
	    Jmol.script(jmolApplet0, 'load ' + initial + ';');
	    
	}
    
    
    
    

    
         //var initial = '"model example"'+$("#initial").val() +' end "model example"';
         
         //console.log(initial);
         
         //Jmol.script(jmolApplet0,'data '+initial+'; show data;' );
         //console.log(initial);
         
         
         //Jmol.script(jmolApplet0,'load '+initial+'; show data;' );
         
         //setTimeout(function() {
            
        //}, 200); 
    
    


});
