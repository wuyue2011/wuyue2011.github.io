$sourcePath = "F:\mtr-nte\build\MTR-NTE-0.5.4+1.18.2.jar"
$destinationPath = "F:\MC\ARAF\mods"
$executablePath = "F:\MC\Plain Craft Launcher 2"

Copy-Item -Path "$sourcePath" -Destination "$destinationPath" -Recurse -Force
Start-Process -FilePath "$executablePath"
#.\run.ps1
#./gradlew build -PbuildVersion="1.18.2"
