call env.cmd
call mvn -e -f ../pom.xml release:prepare || goto :EOF
