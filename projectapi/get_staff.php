<?php
	header("Access-Control-Allow-Origin: *");
	header("Content-Type: application/json; charset=UTF-8");

	$data = json_decode(file_get_contents("php://input"), true);
	$sid   = $data['sid']   ?? '';
	$spass = $data['spassword'] ?? '';

	$conn = new mysqli("localhost", "root", "", "ProjectDB");
	if ($conn->connect_error) die(json_encode(["error" => "DB fail"]));

	$stmt = $conn->prepare("SELECT sid, sname, srole FROM staff WHERE sid=? AND spassword=?");
	$stmt->bind_param("is", $sid, $spass);
	$stmt->execute();
	$res = $stmt->get_result();

	if ($row = $res->fetch_assoc()) {
		echo json_encode(["status"=>"ok","staff"=>$row]);
	} else {
		http_response_code(401);
		echo json_encode(["status"=>"fail","msg"=>"Invalid ID or password"]);
	}
	$stmt->close(); $conn->close();
?>