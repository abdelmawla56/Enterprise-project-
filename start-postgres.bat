@echo off
set PG_PATH="D:\youssef-service 2\youssef-service\pgsql"
%PG_PATH%\bin\pg_ctl.exe -D %PG_PATH%\data -l %PG_PATH%\data\pg_log.txt start
