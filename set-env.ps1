# set-env.ps1 – Load local environment variables for Spring Boot
# Usage: . .\set-env.ps1   (note the leading dot to source into current shell)

$envFile = Join-Path $PSScriptRoot "student-service\.env"

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $name  = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
        Write-Host "  SET $name"
    }
}
Write-Host "`nEnvironment variables loaded. You can now run the Spring Boot service."
