# Using eclipse-temurin with JRE to support several architectures.
FROM eclipse-temurin:21-jre
#Create working folder
RUN mkdir /opt/app
# Copy the compiled jar file into the container
COPY ./hmci-latest.jar /opt/app/
# Run the jar file with default values for config file
CMD ["java", "-jar", "/opt/app/hmci-latest.jar", "-c", "/opt/app/config/hmci.toml"]
