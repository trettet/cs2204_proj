# CS2204 OS Final Project : Priority Preemptive Scheduling Simulation

### Apache instructions:

1. Add the line

> ProxyPass /ajax http://localhost:81/

before the line where you find

> DocumentRoot

on httpd.conf file

2. Uncomment the line

> LoadModule proxy_http_module modules/mod_proxy_http.so 

on the same file to enable the proxy module in apache.

3. Restart Apache

### To run the Backend Java program:

1. Navigate to your jar file
2. Run in the CLI the following commands

> java -jar \<jar-file.jar\>

There shouldn't be any errors when running the jar file via CLI, if you do, terminate any Java VM processes in via the Task Manager
