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


<?php include("jsmol.php"); ?>



</body>
</html>

<?php
$OUTPUT->footer();

