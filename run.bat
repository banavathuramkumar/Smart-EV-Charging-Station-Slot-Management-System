@echo off
setlocal
echo ==============================================================
echo   Launching EV ChargeHub Desktop Application
echo   Smart EV Charging Station & Slot Management System
echo ==============================================================

set JAVA_CMD=java
if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" set JAVA_CMD="C:\Program Files\Java\jdk-26.0.1\bin\java.exe"

%JAVA_CMD% --module-path "lib\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.base,javafx.graphics --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" com.evchargehub.Main

endlocal
