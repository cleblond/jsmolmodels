
<script src="/tsugi/mod/openochem/js/jquery-1.11.1.min.js"></script>
<script src="/tsugi/mod/openochem/js/jquery-ui.min.js"></script>
<script src="/tsugi/mod/openochem/js/kekule_libs/dist/kekule.js?modules=widget&min=true"></script>

<link rel="stylesheet" type="text/css" href="/tsugi/mod/openochem/js/kekule_libs/dist/themes/default/kekule.css" />
<link rel="stylesheet" type="text/css" href="/tsugi/mod/openochem/css/eocustom.css" />
<script type="text/javascript" src="/jmol/jsmol/JSmol.min.js"></script>
<!-- <script type="text/javascript" src="/tsugi/mod/openochem/js/bootstrap.min.js"></script> -->

<link href="/tsugi/mod/openochem/css/bootstrap.min.css" rel="stylesheet">
<script src="/tsugi/mod/openochem/js/bootstrap.min.js"></script>


<script type="text/javascript" src="/tsugi/mod/jsmolmodels/js/jsmol_editor.js"></script>
<script type="text/javascript" src="/tsugi/mod/jsmolmodels/js/bootbox.min.js"></script>





<style type="text/css">
#jsmoldiv {
  width:90%;
  height:65vw;
  max-width:700px;
  max-height:500px;
  display:inline; /* needed for horiz. centering */
}

</style>




 <div class="btn-toolbar" style="padding: 10px;" role="toolbar" aria-label="...">






          <div data-widget="Kekule.Widget.ButtonGroup">
            <button onclick='resetJsmol();' title="Clear Structure" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-NewDoc"></button>
            <button onclick='getUndo()' title="Undo" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-Undo"></button>
            <button onclick='getRedo()' title="Redo" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-Redo" ></button>
            <button onclick='procBtn("zoomI")' title="Zoom In" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-ZoomIn" ></button>
            <button onclick='procBtn("zoomO")' title="Zoom Out" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-ZoomOut" ></button>
            <button onclick='procBtn("rotateB")' title="Rotate about bond/Set dihedral angle" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-RotateLeft" ></button>
            <button onclick='moveMol(1)' title="Move" data-widget="Kekule.Widget.RadioButton" class="Button" ><i class="fa fa-2x fa-arrows-alt"></i></button>
            <button onclick='procBtn("correctH")' title="ADD Hydrogens" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolHideHydrogens"></button>
            <button onclick='procBtn("optimizeM")' title="Optimize Structure" data-widget="Kekule.Widget.RadioButton" class="Button"><i class="fa fa-2x fa-motorcycle"></i></button>
          </div>





		</div>

<div style="width: 100%; white-space: nowrap; display: inline-block;">

          <div data-widget="Kekule.Widget.ButtonGroup" data-layout="2" style="vertical-align: top">
            <button onclick='deleteAtomBond(1)' title="Delete Atom/Bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-BasicMolEraserIaController" ></button>
            <button onclick='procBtn("bond1")' title="Single Bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-MolBondIaController-Single" ></button>
	    <button onclick='procBtn("bond2")'title="Double Bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-MolBondIaController-Double" ></button>
	    <button onclick='procBtn("bond3")' title="Triple bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-MolBondIaController-Triple" ></button>
            <button onclick='procBtn("atomH")' title="Add H" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >H</button>
            <button onclick='procBtn("atomC")' title="Add C" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >C</button>
            <button onclick='procBtn("atomN")' title="Add N" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >N</button>
	    <button onclick='procBtn("atomO")' title="Add O" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >O</button>
            <button onclick='procBtn("atomCl")' title="Add Cl" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >Cl</button>
            <button onclick='osrP("atomX")' title="Add other atoms" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >X</button>
            
            <button id="ringbtn" class="GlyphButton" title="Ring structures tool" data-widget="Kekule.Widget.CompactButtonSet" data-text="Button4" data-show-text="false" data-button-set="#radioButtonGroup2" class=" K-Widget K-Button K-NonSelectable K-No-Wrap K-Text-Hide K-Glyph-Show K-Layout-H K-Chem-MolRingIaController"></button>
            

            
            
          </div>


            <div id="radioButtonGroup2" data-widget="Kekule.Widget.ButtonGroup" data-layout="1" style="horizontal-align: top">
            <button onclick='procBtn("ring3")'  data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-3" ></button>
            <button onclick='procBtn("ring4")'  data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-4" ></button>            
            <button onclick='procBtn("ring5")'  data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-5" ></button>            
            <button onclick='procBtn("ring6")'  data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-6" ></button>     
            <button onclick='procBtn("ringar6")' data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-Ar-6" data-checked="true"></button>                   
          </div>

          
        <div id="jsmoldiv" style="display: inline-block; z-index: 400;">
        
        </div>



</div>
      <!-- <div class="col-xs-11"> -->

      
