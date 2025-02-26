$sourcePath = "F:\mtr-ante\build\MTR-ANTE-1.0.2+1.18.2.jar"
$destinationPath = "F:\mc\araf\versions\1.18.2-Fabric 0.16.9\mods"
$executablePath = "F:\mc\Plain Craft Launcher 2"

Copy-Item -Path "$sourcePath" -Destination "$destinationPath" -Recurse -Force
Start-Process -FilePath "$executablePath"
#.\run.ps1
#./gradlew build -PbuildVersion="1.18.2"
