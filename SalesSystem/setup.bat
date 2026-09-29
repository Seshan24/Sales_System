@echo off
REM ---------------------------------------------------------------
REM Sales Management System - Windows setup (creates DB, builds, deploys to dist\)
REM Requirements: MySQL Server (mysql.exe in PATH), JDK 11+ and Maven (mvn in PATH)
REM ---------------------------------------------------------------
cd /d "%~dp0"
REM ask for the MySQL credentials
set /p DBUSER=MySQL user (e.g. root): 
set /p DBPASS=MySQL password: 
REM create the database, tables and sample data
echo Creating database...
mysql -u%DBUSER% -p%DBPASS% < database\sales_db.sql
IF ERRORLEVEL 1 (
  echo ERROR: database creation failed. Check MySQL is running and the credentials are correct.
  pause
  exit /b 1
)
REM compile the project and collect all libraries
echo Building application...
call mvn -q clean package
IF ERRORLEVEL 1 (
  echo ERROR: Maven build failed.
  pause
  exit /b 1
)
REM assemble the deployable folder
if exist dist rmdir /s /q dist
mkdir dist
copy /y target\sales-system-1.0.jar dist\ >nul
xcopy /e /i /y target\lib dist\lib >nul
REM write the database settings next to the jar
(echo db.url=jdbc:mysql://localhost:3306/sales_db?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC)> dist\db.properties
(echo db.user=%DBUSER%)>> dist\db.properties
(echo db.password=%DBPASS%)>> dist\db.properties
REM create the launcher
(echo @echo off)> dist\run.bat
(echo cd /d "%%~dp0")>> dist\run.bat
(echo java -jar sales-system-1.0.jar)>> dist\run.bat
echo.
echo SETUP COMPLETE. Start the application with dist\run.bat
echo Login: admin / admin123   or   cashier1 / cashier123
pause
