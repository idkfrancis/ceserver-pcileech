call env.cmd
call mvn -e -f ../pom.xml release:perform || goto :EOF
