@echo off
setlocal
cd /d "%~dp0.."
where py >nul 2>nul
if %errorlevel%==0 (
  py -3 scripts\audit_hytale_assets.py %*
  exit /b %errorlevel%
)
where python >nul 2>nul
if %errorlevel%==0 (
  python scripts\audit_hytale_assets.py %*
  exit /b %errorlevel%
)
echo FAIL AUDIT: Python 3 was not found.
echo Install Python 3 or run scripts\audit_hytale_assets.py with a Python 3 interpreter.
exit /b 1
