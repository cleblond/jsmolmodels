<?php

require_once "config.php";
//require_once "../libs.php";

use \Tsugi\Core\LTIX;

$LTI = LTIX::requireData();
$p = $CFG->dbprefix;

echo"<pre>";
var_dump($_POST);
echo"</pre>";


if ($USER->instructor) {

     if ( isset($_POST)) {
           //if ($_POST['action'] == "save") {
                 if ($_POST['initial']) {
                      $initial = $_POST['initial'];
                 
                 } 
             
                 //setting
                 //$settings = new stdClass();
                 //$settings->allow_add_atoms = isset($_POST['allow_add_atoms']) ? 1 : 0; 
                 //$settings->show_examples = isset($_POST['show_examples'])  ? 1 : 0;
                 //$settings->mode = $_POST['mode'] == 'quiz'  ? 1 : 0;
                 
                 //save the initial and answer   
                 $query = $PDOX->queryDie("INSERT INTO {$p}jsmolmodels
                        (link_id, user_id, initial)
                        VALUES ( :LI, :UI, :IN)
                        ON DUPLICATE KEY UPDATE initial=:IN",
                        array(
                            ':LI' => $LINK->id,
                            ':UI' => $USER->id,
                            ':IN' => $initial
                            //':AN' => $_POST["answer"],
                            //':ST' => json_encode($settings)
                        )
                    );
             
                 if ($query) {
                        echo "true";
                 } else {
                        echo "false";
                 
                 }
          
     }
}
