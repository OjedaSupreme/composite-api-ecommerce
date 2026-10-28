# Diagnostico del entorno de desarrollo (Java, Maven, Git, GitHub CLI)
$out = Join-Path $PSScriptRoot "..\logs\diagnostico.txt"
New-Item -ItemType Directory -Force -Path (Split-Path $out) | Out-Null
"=== $(Get-Date) ===" | Out-File $out -Encoding utf8
foreach ($c in @("java -version","javac -version","mvn -v","git --version","gh --version","gh auth status","git config --global user.name","git config --global user.email","where.exe java","where.exe mvn","where.exe gh")) {
  "`n>>> $c" | Out-File $out -Append -Encoding utf8
  try { cmd /c "$c 2>&1" | Out-File $out -Append -Encoding utf8 } catch { "ERROR: $_" | Out-File $out -Append -Encoding utf8 }
}
"`n>>> JAVA_HOME=$env:JAVA_HOME" | Out-File $out -Append -Encoding utf8
Get-ChildItem "C:\Program Files\Java","C:\Program Files\Eclipse Adoptium","C:\Program Files\Microsoft","$env:USERPROFILE\.jdks" -ErrorAction SilentlyContinue | Select-Object FullName | Out-File $out -Append -Encoding utf8
Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot" -ErrorAction SilentlyContinue | Select-Object Name | Out-File $out -Append -Encoding utf8
Write-Host "Diagnostico guardado en logs\diagnostico.txt"
