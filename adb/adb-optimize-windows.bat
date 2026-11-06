@echo off
setlocal
echo Performance Booster - ajustes ADB seguros
echo Conecte o Android com Depuracao USB ativada e aceite a chave RSA.
adb devices
if errorlevel 1 goto erro
echo.
echo Aplicando animacoes em 0.5x...
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5
echo.
echo Diagnostico:
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell dumpsys meminfo
echo.
echo Concluido. Para restaurar animacoes, execute restore-defaults-windows.bat
pause
exit /b 0
:erro
echo Falha. Instale Android Platform Tools e adicione adb ao PATH.
pause
exit /b 1
