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
$OUTPUT->flashMessages();
?>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <title>JSMol Model Kit</title>
    
    <style type="text/css">
    </style>

</head>

<body>


<?php include("jsmol.php"); 

//$initial = '';
?>


   <input type="hidden" id="ajax" name="ajax" value="<?php echo(addSession('ajax.php'))?>">
   
   <div <?=$inst?> >
   <form id="setupform" action="index.php" method="post">   
            

                    
            <input class="btn btn-xs btn-primary" type="submit" name="save" value="Save">
        
            <div id="instr_cntrls" class="form-group" >
            
                    <div class="row">



                      <div class="col-xs-6">


                        <button class="btn btn-xs btn-primary" id="initialbtn">Set Initial</button>
                        <button class="btn btn-xs btn-primary" id="viewinitialbtn">View Initial</button>
                        <textarea class="form-control" name="initial" id="initial"><?=$initial?></textarea>
                        

                        

                      </div> 
                        
                    </div>
                    
                    

               
            </div>  
    </form>
    </div>


</body>
</html>

<?php
$OUTPUT->footer();

