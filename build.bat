@echo off
setlocal
echo ==============================================================
echo   Building EV ChargeHub Enterprise Desktop Application
echo ==============================================================

if not exist "bin" mkdir bin
if not exist "data" mkdir data
if not exist "reports" mkdir reports

powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1

endlocal
