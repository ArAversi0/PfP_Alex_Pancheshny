$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Push-Location $projectRoot
try {
    if (-not $env:JAVA_HOME) {
        $defaultJava = "C:\Users\user\.jdks\corretto-23.0.2"
        if (Test-Path $defaultJava) {
            $env:JAVA_HOME = $defaultJava
        }
    }

    Write-Host "[PfP] Building backend jar..."
    .\gradlew.bat :apps:backend:bootJar --console=plain

    Write-Host "[PfP] Building web dist..."
    Push-Location apps\web
    try {
        npm run build
    } finally {
        Pop-Location
    }

    $composeArgs = @()
    if (Test-Path ".env") {
        Write-Host "[PfP] Loading environment from .env"
        $composeArgs += @("--env-file", ".env")
    } else {
        Write-Host "[PfP] .env was not found; Google OAuth2 stays disabled and local JWT fallback is used."
    }

    Write-Host "[PfP] Starting Docker Compose stack..."
    docker compose @composeArgs -f infra\compose.yaml up -d --build

    Write-Host "[PfP] Stack is starting. Web: http://localhost:5173, API: http://localhost:8080, Mailpit: http://localhost:8025"
} finally {
    Pop-Location
}