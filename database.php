<?php

// The SQL to uninstall this tool
$DATABASE_UNINSTALL = array(
"drop table if exists {$CFG->dbprefix}jsmolmodels"
);

// The SQL to create the tables if they don't exist
$DATABASE_INSTALL = array(

array( "{$CFG->dbprefix}jsmolmodels",
"create table {$CFG->dbprefix}jsmolmodels (
    link_id     INTEGER NOT NULL,
    user_id     INTEGER NOT NULL,
    initial     TEXT NOT NULL,
    
    UNIQUE(link_id, user_id)
    
) ENGINE = InnoDB DEFAULT CHARSET=utf8")

);


