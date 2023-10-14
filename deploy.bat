@echo off

rem Run Maven Clean and Package
mvn clean package

echo Copy files...

rem Use PSCP (PuTTY Secure Copy) to copy the JAR file to the remote server
pscp -i C:\Users\assemgul\.ssh\id_rsa target\rt-charity-remainder-bot-0.0.1-SNAPSHOT.jar root@143.198.157.180:/rt-charity/

echo Restart server...

rem Use Plink (PuTTY Link) to execute commands on the remote server
plink -i C:\Users\assemgul\.ssh\id_rsa root@143.198.157.180 << EOF

REM Kill existing Java process
TASKKILL /IM java.exe /F

REM Start the Java application
java -jar rt-charity-remainder-bot-0.0.1-SNAPSHOT.jar > log.txt &

EOF

echo Bye