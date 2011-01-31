@ECHO OFF 

set OSGI_IMPL=org.eclipse.osgi-3.6.0.v20100517.jar
set OSE_HOME=.
REM set OSGI_IMPL=org.eclipse.osgi_3.4.3.R34x_v20081215-1030.jar
java   ^
		-Djetty.home="%OSE_HOME%" ^
			-Djetty.port=8282 ^
			-Djetty.logs="%OSE_HOME%/logs" ^
		-Dcom.sun.management.jmxremote ^
			-Dcom.sun.management.jmxremote.port=5222 ^
			-Dcom.sun.management.jmxremote.authenticate=false ^
			-Dcom.sun.management.jmxremote.ssl=false ^
		-jar %OSGI_IMPL% -clean -console

pause