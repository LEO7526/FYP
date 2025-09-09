<?php
// Database connection settings
$hostname = "127.0.0.1";
$username = "root";
$password = "";
$database = "projectdb";

// Create the database connection
$conn = mysqli_connect($hostname, $username, $password, $database) or die(mysqli_connect_error());
?>