$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ToolsRoot = Join-Path $ProjectRoot ".tools"

$JavaRoot = Join-Path $ToolsRoot "jdk-25"
$GradleVersion = "9.9.1"
$GradleRoot = Join-Path $ToolsRoot "gradle-$GradleVersion"

$GradleExe = Join-Path $GradleRoot "bin\gradle.bat"

function Find-JavaHome {
    if (Test-Path $JavaRoot) {
        $JavaExe = Get-ChildItem $JavaRoot -Filter java.exe -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -match '\\bin\\java\.exe$' } |
            Select-Object -First 1

        if ($JavaExe) {
            return (Split-Path (Split-Path $JavaExe.FullName -Parent) -Parent)
        }
    }

    return $null
}

New-Item -ItemType Directory -Force -Path $ToolsRoot | Out-Null

$JavaHome = Find-JavaHome

if (-not $JavaHome) {
    Write-Host "[EaglePad] Downloading portable JDK 25..."

    $JavaZip = Join-Path $ToolsRoot "jdk25.zip"
    $JdkUrl = "https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jdk/hotspot/normal/eclipse"

    Invoke-WebRequest -Uri $JdkUrl -OutFile $JavaZip

    New-Item -ItemType Directory -Force -Path $JavaRoot | Out-Null
    Expand-Archive -Path $JavaZip -DestinationPath $JavaRoot -Force
    Remove-Item $JavaZip -Force

    $JavaHome = Find-JavaHome

    if (-not $JavaHome) {
        throw "Could not locate Java after extraction."
    }
}

$env:JAVA_HOME = $JavaHome
$env:PATH = (Join-Path $JavaHome "bin") + ";" + $env:PATH

if (-not (Test-Path $GradleExe)) {
    Write-Host "[EaglePad] Downloading Gradle $GradleVersion..."

    $GradleZip = Join-Path $ToolsRoot "gradle-$GradleVersion-bin.zip"

    Invoke-WebRequest `
        -Uri "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip" `
        -OutFile $GradleZip

    Expand-Archive -Path $GradleZip -DestinationPath $ToolsRoot -Force
    Remove-Item $GradleZip -Force
}

if (-not (Test-Path $GradleExe)) {
    throw "Gradle executable not found."
}

Write-Host ""
Write-Host "========== EAGLEPAD BUILD ==========" -ForegroundColor Cyan
Write-Host "Java: $JavaHome"
Write-Host "Gradle: $GradleVersion"

& (Join-Path $JavaHome "bin\java.exe") -version

Push-Location $ProjectRoot

try {
    Write-Host ""
    Write-Host "[EaglePad] Compiling project..."

    & $GradleExe --no-daemon --no-configuration-cache build

    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

Write-Host ""
Write-Host "BUILD COMPLETE" -ForegroundColor Green

$VersionLine = Select-String `
    -Path (Join-Path $ProjectRoot "gradle.properties") `
    -Pattern '^mod_version='

if ($VersionLine) {
    $ModVersion = $VersionLine.Line.Split('=')[1].Trim()
    $Jar = Join-Path $ProjectRoot "build\libs\eaglepad-$ModVersion.jar"

    if (Test-Path $Jar) {
        Write-Host "Mod JAR: $Jar" -ForegroundColor Green
        Get-Item $Jar | Format-List FullName, Length
    }
    else {
        Write-Warning "Build succeeded, but expected JAR was not found: $Jar"
    }
}