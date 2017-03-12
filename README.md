# CS2204 OS Final Project : Priority Preemptive Scheduling Simulation

### Apache instructions:

1. Copy the folder to /htdocs on xampp

2. Add the line

> ProxyPass /ajax http://localhost:81/

before the line where you find

> DocumentRoot

on httpd.conf file

3. Uncomment the line

> LoadModule proxy_http_module modules/mod_proxy_http.so 

on the same file to enable the proxy module in apache.

4. Restart Apache

### To run the Backend Java program:

1. Navigate to the jar file located on ("\priopreemptive\CS2204_Final_Proj_Backend\out\artifacts\CS2204_Final_Proj_Backend_jar") using the Command Prompt
2. Run in the CLI the following commands

> java -jar \<jar-file.jar\>

There shouldn't be any errors when running the jar file via CLI, if you do, terminate any Java VM processes in via the Task Manager
