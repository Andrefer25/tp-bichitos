param([switch]$Test,[switch]$InstallDemo)
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    New-Item -ItemType Directory -Force out | Out-Null
    $sourceFiles = Get-ChildItem src/main/java,src/test/java -Recurse -Filter '*.java' | ForEach-Object FullName
    & javac -encoding UTF-8 -d out $sourceFiles
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación' }
    $classPath = 'out;lib/mysql-connector-j-8.0.33.jar'
    if ($InstallDemo) {
        & java '-Dfile.encoding=UTF-8' -cp $classPath org.bichitos.repository.InstallDemo
    } elseif ($Test) {
        & java '-Dfile.encoding=UTF-8' -cp $classPath org.bichitos.SmokeTest
        if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas unitarias' }
        & java '-Dfile.encoding=UTF-8' -cp $classPath org.bichitos.desktop.DesktopAcceptanceTest
    } else {
        & java '-Dfile.encoding=UTF-8' -cp $classPath org.bichitos.desktop.App
    }
    if ($LASTEXITCODE -ne 0) { throw 'La ejecución terminó con error' }
} finally { Pop-Location }
