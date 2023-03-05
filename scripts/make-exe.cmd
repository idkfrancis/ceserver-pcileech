call env.cmd
rd /q /s ..\target
mkdir ..\target || goto :EOF
call mvn -e -f ../pom.xml package || goto :EOF
jlink --output ..\target\jpackage-runtime --add-modules java.base,java.desktop,java.logging
mkdir ..\target\jpackage-temp || goto :EOF
mkdir ..\target\jpackage-out || goto :EOF
copy ..\target\ceserver-pcileech.jar ..\target\jpackage-temp || goto :EOF
C:\Users\iflores\.jdks\temurin-17.0.2\bin\jpackage @make-exe.opts || goto :EOF
echo ******************
echo * BUILD COMPLETE *
echo ******************
