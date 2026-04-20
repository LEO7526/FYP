@echo off
echo Starting Automatic Inventory Reorder...
curl "http://localhost/androidstaff_api/inventory/auto_reorder.php"
echo.
echo ---------------------------------------
echo Process Finished!
pause