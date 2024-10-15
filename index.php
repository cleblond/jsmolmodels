<?php
require_once "../config.php";
// The Tsugi PHP API Documentation is available at:
// http://do1.dr-chuck.com/tsugi/phpdoc/namespaces/Tsugi.html
use \Tsugi\Core\Settings;
use \Tsugi\Core\LTIX;
// No parameter means we require CONTEXT, USER, and LINK
$LAUNCH = LTIX::requireData(); 
// Model
$p = $CFG->dbprefix;
// View

if ($USER->instructor) {

        //select the initial and answer  
     
            $row = $PDOX->rowDie("SELECT * FROM {$p}jsmolmodels WHERE user_id = :UI and link_id = :LI",
                array(
                    ':LI' => $LINK->id,
                    ':UI' => $USER->id
                )
            );
            
            if ($row) {

                $initial = $row['initial'];
                
                $stereo =  $row['showstereo'] ? "checked" : "";
                $search =  $row['showsearch'] ? "checked" : "";
                $sterval = $row['showstereo'];
                $searval = $row['showsearch'];   
            
                
            
            } else {

                $initial = '';
                $stereo =  "checked";
                $search =  "checked";
                $sterval = 0;
                $searval = 0;   
            
            }
            
            $inst = '';
            
            
     
                       

} else {

            
            $row = $PDOX->rowDie("SELECT initial, showstereo, showsearch FROM {$p}jsmolmodels WHERE link_id = :LI",
                array(
                    ':LI' => $LINK->id
                )
            );

            if ($row) {
                $initial = $row['initial'];
                $stereo =  $row['showstereo'] ? "checked" : "";
                $search =  $row['showsearch'] ? "checked" : "";
                $sterval = $row['showstereo'];
                $searval = $row['showsearch'];
            } else {
                    $initial = false;
                    $sterval = 1;
                    $searval = 1;
            }
            
            $inst = "style='display: none;'";
            
}


$OUTPUT->header();
$OUTPUT->bodyStart();

?>

<script>

var initial = `<?=$initial?>`;
var showstereo = <?=$sterval?>;
var showsearch = <?=$searval?>;

//console.log(showsearch);

</script>


<div class="container">

<?php  if ($USER->instructor) { ?>

<a href="#" id="toggle-settings" class="btn btn-primary">Show Settings</a>




<!DOCTYPE html>
<div class="row" >
    <div class="col-md-10">
        <div class="well" id="settings" style="display:none; margin-top: 20px;" >


            <input type="hidden" id="ajax" name="ajax" value="<?php echo(addSession('ajax.php'))?>">

            <div <?=$inst?> >

             



            
                        <form class="form-horizontal" id="setupform" action="index.php" method="post">   
                            <input type="checkbox" id="showstereo" name="showstereo" value="showstereo" <?php echo $stereo;?>>
<label >Show Stereochemistry</label><br>

                            <input type="checkbox" id="showsearch" name="showsearch" value="showsearch" <?php echo $search?>>
<label >Show Search Boxes</label><br>


                            <div class="form-group" >
                                 <label>Construct an initial molecule below, click "Set Initial", then click "Save".</small></label><br>
                                <button class="btn btn-primary" id="initialbtn">Set Initial</button>
                                <button class="btn btn-primary" id="viewinitialbtn">View Initial</button>
                                <input class="btn btn-success pull-right" type="submit" name="save" value="Save">
                            </div>
                            <div class="form-group" >
                                <label for="initial">Initial molfile <small>(if not set methane will be displayed)</small></label>
                                <textarea class="form-control" name="initial" id="initial"><?=$initial?></textarea>

                            </div> 
                                        
                              
                        </form>                   
                        

            </div>
        </div>
    </div>
</div>


<?php
  } else {
?>
  
  
 <textarea style="display:none" class="form-control" name="initial" id="initial"><?=$initial?></textarea>

  
<?php }

//echo $initial;

include("jsmol.php");

$OUTPUT->footer();

?>


<?php
 

