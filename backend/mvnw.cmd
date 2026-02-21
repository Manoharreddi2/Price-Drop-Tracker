<# : batch script
@ECHO OFF
@REM Maven Wrapper script for Windows (mvnw.cmd)
@REM Downloads Maven if not present and runs it

@setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPERJAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\apache-maven-3.9.6

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven 3.9.6...
    if not exist "%MAVEN_PROJECTBASEDIR%.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%.mvn\wrapper"
    powershell -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven.zip'"
    powershell -Command "Expand-Archive -Path '%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven.zip' -DestinationPath '%MAVEN_PROJECTBASEDIR%.mvn\wrapper' -Force"
    del "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven.zip"
)

set PATH=%MAVEN_HOME%\bin;%PATH%
mvn.cmd %*

@endlocal
goto :eof
#>
# PowerShell version
$MAVEN_PROJECTBASEDIR = $PSScriptRoot
$MAVEN_HOME = Join-Path $MAVEN_PROJECTBASEDIR ".mvn\wrapper\apache-maven-3.9.6"
$MAVEN_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"

if (-not (Test-Path "$MAVEN_HOME\bin\mvn.cmd")) {
    Write-Host "Downloading Maven 3.9.6..."
    $wrapperDir = Join-Path $MAVEN_PROJECTBASEDIR ".mvn\wrapper"
    if (-not (Test-Path $wrapperDir)) { New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null }
    $zipPath = Join-Path $wrapperDir "maven.zip"
    Invoke-WebRequest -Uri $MAVEN_URL -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $wrapperDir -Force
    Remove-Item $zipPath
}

$env:PATH = "$MAVEN_HOME\bin;$env:PATH"
& mvn.cmd @args
