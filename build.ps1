$gradlewPath = "F:\mtr-nte\gradlew.bat"
Start-Process -FilePath "$gradlewPath" -ArgumentList "build", "-PbuildVersion=1.18.2", "--scan" -NoNewWindow -Wait
