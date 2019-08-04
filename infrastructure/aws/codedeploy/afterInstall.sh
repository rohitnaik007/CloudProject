#!/bin/bash

# update the permission and ownership of WAR file in the tomcat webapps directory

sudo chmod 775 -R /var/lib/tomcat8/webapps
sudo chown tomcat8:tomcat8 -R /var/lib/tomcat8/webapps
sudo service tomcat8 stop

sudo chown -R ubuntu:ubuntu /etc/default/tomcat8
sudo chown -R tomcat8:tomcat8 /var/lib/tomcat8

sudo echo 'JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=aws"' >> /etc/default/tomcat8
