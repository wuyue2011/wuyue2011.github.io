$gradlewPath = "F:\mtr-nte\gradlew.bat"
Start-Process -FilePath "$gradlewPath" -ArgumentList "build", "-PbuildVersion=1.18.2", "-Dorg.gradle.internal.http.socketTimeout=200000", "-Dorg.gradle.internal.http.connectionTimeout=200000", "--warning-mode all" -NoNewWindow -Wait
