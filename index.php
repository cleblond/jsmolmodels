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
            
            } else {

                $initial = '';
            
            }
            
            $inst = '';
     
                       

} else {

            
            $row = $PDOX->rowDie("SELECT initial FROM {$p}jsmolmodels WHERE link_id = :LI",
                array(
                    ':LI' => $LINK->id
                )
            );

            if ($row) {
                    $initial = $row['initial'];
                    
                    
                    //var_dump($answer);
            } else {
                    $initial = false;
            }
            
            $inst = "style='display: none;'";
            
}


$OUTPUT->header();
$OUTPUT->bodyStart();

?>
<div class="container">

<?php  if ($USER->instructor) { ?>
<h3>Initial Settings</h3>
<!DOCTYPE html>
<div class="row">
    <div class="col-md-10">
        <div class="well">


            <input type="hidden" id="ajax" name="ajax" value="<?php echo(addSession('ajax.php'))?>">

            <div <?=$inst?> >

             



            
                        <form class="form-horizontal" id="setupform" action="index.php" method="post">   
                        
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

include("jsmol.php"); 

$OUTPUT->footer();

