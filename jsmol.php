
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


  <div  style="margin: 0.5em; width: 100%; white-space: nowrap; display: inline-block;" role="toolbar" aria-label="...">


          <div data-widget="Kekule.Widget.ButtonGroup">
            <a onclick='resetJsmol();' title="Clear Structure" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-NewDoc" formnovalidate></a>
            <a onclick='getUndo()' title="Undo" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-Undo" formnovalidate></a>
            <a onclick='getRedo()' title="Redo" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-Redo" ></a>
            <a onclick='procBtn("zoomI")' title="Zoom In" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-ZoomIn" ></a>
            <a onclick='procBtn("zoomO")' title="Zoom Out" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-ZoomOut" ></a>
            

            
          </div>





    </div>

    <div style="margin-right: 0.5em; margin-left: 0.5em; width: 100%; white-space: nowrap; display: inline-block;">

          <div data-widget="Kekule.Widget.ButtonGroup" data-layout="2" style="vertical-align: top">
            <a onclick='deleteAtomBond(1)' title="Delete Atom/Bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-BasicMolEraserIaController" ></a>

        
            <a id="atombtn"  title="Atom tool" data-widget="Kekule.Widget.CompactButtonSet"  data-button-set="#radioButtonGroup0"></a>
            
            <a id="bondbtn" class="GlyphButton" title="Bond tool" data-widget="Kekule.Widget.CompactButtonSet"  data-button-set="#radioButtonGroup1"></a>
            
            <a id="ringbtn" class="GlyphButton" title="Ring structures tool" data-widget="Kekule.Widget.CompactButtonSet" data-text="Button4" data-show-text="false" data-button-set="#radioButtonGroup2"></a>
            
            <a onclick='procBtn("correctH")' title="ADD Hydrogens" data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolHideHydrogens"></a>
            
            <a onclick='moveMol(1)' title="Move atoms or structure" data-widget="Kekule.Widget.RadioButton" class="Button" ><i class="fa fa-2x fa-arrows-alt"></i></a>

            <a onclick='procBtn("rotateB")' title="Rotate about bond/Set dihedral angle" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-RotateLeft" ></a>  
            <a onclick='procBtn("optimizeM")' title="Optimize Structure" data-widget="Kekule.Widget.RadioButton" class="Button"><i class="fa fa-2x fa-motorcycle"></i></a>
            
          </div>
          
          
          <div id="radioButtonGroup0" data-widget="Kekule.Widget.ButtonGroup" data-layout="1" style="horizontal-align: top">
          
            <a onclick='procBtn("atomH")' title="Add H" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >H</a>
            <a onclick='procBtn("atomC")' title="Add C" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" data-checked="true">C</a>
            <a onclick='procBtn("atomN")' title="Add N" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >N</a>
	        <a onclick='procBtn("atomO")' title="Add O" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >O</a>
            <a onclick='procBtn("atomCl")' title="Add Cl" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >Cl</a>
            <a onclick='osrP("atomX")' title="Add other atoms" data-widget="Kekule.Widget.RadioButton"  class="Button atombtn" >X</a>
          
          </div> 
          
          
          
          
          <div id="radioButtonGroup1" data-widget="Kekule.Widget.ButtonGroup" data-layout="1" style="horizontal-align: top">
          
            <a onclick='procBtn("bond1")' title="Single Bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-MolBondIaController-Single" data-checked="true" ></a>
	        <a onclick='procBtn("bond2")' title="Double Bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-MolBondIaController-Double" ></a>
	        <a onclick='procBtn("bond3")' title="Triple bond" data-widget="Kekule.Widget.RadioButton"  class="GlyphButton K-Chem-MolBondIaController-Triple" ></a>
          
          
          </div>   
          
          <div id="radioButtonGroup2" data-widget="Kekule.Widget.ButtonGroup" data-layout="1" style="horizontal-align: top">
            <a onclick='procBtn("ring3")' data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-3" ></a>
            <a onclick='procBtn("ring4")' data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-4" ></a>            
            <a onclick='procBtn("ring5")' data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-5" ></a>            
            <a onclick='procBtn("ring6")' data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-6" ></a>     
            <a onclick='procBtn("ringar6")' data-widget="Kekule.Widget.RadioButton" class="GlyphButton K-Chem-MolRingIaController-Ar-6" data-checked="true"></a>                   
          </div>



        <div id="jsmoldiv" style="display: inline-block; z-index: 400;"></div>

    </div>
      <!-- <div class="col-xs-11"> -->

      
