FROM tomcat:9.0

LABEL maintainer="victorjaramillo"

ADD target/ferme_front-0.0.1.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]