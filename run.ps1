$sourcePath = "F:\mtr-nte\build\MTR-NTE-0.5.4+1.18.2.jar"
$destinationPath = "F:\MC\H1\ARAF\mods"
$executablePath = "F:\MC\H1\HMCL-3.6.11.exe"

Copy-Item -Path "$sourcePath" -Destination "$destinationPath" -Recurse -Force
Start-Process -FilePath "$executablePath"
#.\run.ps1
#./gradlew build -PbuildVersion="1.18.2"
