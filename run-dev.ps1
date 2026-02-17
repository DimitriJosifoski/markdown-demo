# Load .env and start the app
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#=].+?)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}
mvn spring-boot:run
