REM Shell script to create an instance of the 3D City Database
REM on PostgreSQL/PostGIS

REM Provide your database details here
set PGPORT=54321
set PGHOST=localhost
set PGUSER=postgres
set CITYDB=3dcitydb
set PGBIN=C:\Program Files\OpenGeo\OpenGeo Suite\pgsql\9.1\bin\

REM cd to path of the shell script
cd /d %~dp0

REM Run CREATE_DB.sql to create the 3D City Database instance
"%PGBIN%\psql" -d "%CITYDB%" -f "CREATE_DB.sql"

pause