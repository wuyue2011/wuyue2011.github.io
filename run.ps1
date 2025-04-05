$sourcePath = "F:\mtr-ante\build\MTR-ANTE-1.0.4+1.18.2.jar"
$destinationPath = "F:\mc\araf\versions\1.18.2-Fabric 0.16.9\mods"
$executablePath = "F:\mc\run.bat"

Copy-Item -Path "$sourcePath" -Destination "$destinationPath" -Recurse -Force
Start-Process -FilePath powershell.exe -ArgumentList "-Command `& `'$executablePath`'"