Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "  Building EV ChargeHub Enterprise Desktop Application" -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (!(Test-Path "data")) { New-Item -ItemType Directory -Path "data" | Out-Null }
if (!(Test-Path "reports")) { New-Item -ItemType Directory -Path "reports" | Out-Null }

$javacCmd = "javac"
if (Test-Path "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe") {
    $javacCmd = "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe"
}

$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
Write-Host "Compiling $($javaFiles.Count) Java source files with $javacCmd..." -ForegroundColor Yellow

& $javacCmd --release 21 -d bin --module-path "lib\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.base,javafx.graphics -cp "bin;lib\*;src" -nowarn $javaFiles

if ($LASTEXITCODE -eq 0) {
    Copy-Item -Path "resources" -Destination "bin" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "`nBUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "Execute ./run.ps1 or press F5 in VS Code to launch the application." -ForegroundColor Green
} else {
    Write-Host "`nBUILD FAILED with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
