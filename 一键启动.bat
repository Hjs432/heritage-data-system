@echo off
"%SystemRoot%\System32\chcp.com" 936 >nul
title 非遗文化资源管理系统 - 一键启动
setlocal

set "ROOT=%~dp0"
set "JAVA=C:\Users\32987\.jdks\ms-21.0.11\bin\java.exe"
set "NODEDIR=C:\Users\32987\.workbuddy\binaries\node\versions\22.22.2-2"
set "JAR=%ROOT%heritage-java-backend\target\heritage-java-backend.jar"
set "FE=%ROOT%heritage-frontend"

rem 把 Node/npm 目录临时加入 PATH，供本脚本及子窗口使用
set "PATH=%NODEDIR%;%PATH%"

echo ============================================
echo   非遗文化资源管理系统  一键启动
echo ============================================
echo.

echo [1/3] 检查 MongoDB 服务 ...
"%SystemRoot%\System32\sc.exe" query MongoDB | "%SystemRoot%\System32\findstr.exe" /i RUNNING >nul 2>&1
if errorlevel 1 (
    echo       未运行，尝试启动 ...
    "%SystemRoot%\System32\net.exe" start MongoDB
    if errorlevel 1 echo       [提示] 启动失败，请用管理员身份重试：net start MongoDB
) else (
    echo       已在运行
)

echo [2/3] 启动后端（端口 8080）...
if not exist "%JAVA%" (
    echo       [错误] 找不到 JDK：%JAVA%
    pause
    exit /b 1
)
if not exist "%JAR%" (
    echo       [错误] 找不到后端 jar：%JAR%
    echo       请先用 Maven 构建后端后再运行本脚本。
    pause
    exit /b 1
)
start "后端 8080" cmd /k ""%JAVA%" -jar "%JAR%""

echo [3/3] 启动前端（端口 5173）...
if not exist "%FE%\node_modules" (
    echo       未检测到 node_modules，正在安装依赖 ...
    pushd "%FE%"
    call npm install
    popd
)
start "前端 5173" cmd /k "cd /d "%FE%" && npm run dev"

echo.
echo 启动完成。稍等几秒后浏览器会自动打开：http://localhost:5173/
echo 登录账号：admin / 123456（管理员）   user / 123456（只读）
echo 提示：关闭弹出的两个窗口即可停止对应服务。
echo.
"%SystemRoot%\System32\timeout.exe" /t 6 /nobreak >nul
start "" "http://localhost:5173/"

endlocal
