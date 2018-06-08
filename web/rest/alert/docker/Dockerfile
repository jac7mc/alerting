FROM tomcat
MAINTAINER Jeff Chiang


ADD maven/* webapps/
ADD server.xml conf/server.xml
ADD context.xml conf/context.xml

CMD ["catalina.sh", "run"]