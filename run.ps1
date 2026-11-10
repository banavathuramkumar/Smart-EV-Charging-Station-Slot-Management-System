Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  Launching EV ChargeHub Desktop Application" -ForegroundColor Green
Write-Host "  Smart EV Charging Station & Slot Management System" -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green

$javaCmd = "java"
if (Test-Path "C:\Program Files\Java\jdk-26.0.1\bin\java.exe") {
    $javaCmd = "C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
}

& $javaCmd --module-path "lib\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.base,javafx.graphics --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" com.evchargehub.Main
