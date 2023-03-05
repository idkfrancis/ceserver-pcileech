call env.cmd
call mvn -e -f ../pom.xml release:rollback || goto :EOF
