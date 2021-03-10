FROM bde2020/spark-java-template:3.0.1-hadoop3.2

ENV SPARK_APPLICATION_MAIN_CLASS project1.CitiBikeAnalyzer
ENV SPARK_APPLICATION_JAR_NAME spark-lab-1.0-SNAPSHOT
COPY wait-for.sh /
RUN chmod +x /wait-for.sh

ENTRYPOINT ["sh", "-c", "/wait-for.sh ${ES_HEALTHCHECK_ENDPOINT} /template.sh"]
