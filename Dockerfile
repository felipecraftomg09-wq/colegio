# Imagen base con Java 21
FROM eclipse-temurin:21-jdk

# Configurar el directorio de trabajo
WORKDIR /app

# Copiar el archivo pom.xml y descargar dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer el puerto que usará la app
EXPOSE 8080

# Configurar variable de entorno para perfil de producción
ENV SPRING_PROFILES_ACTIVE=prod

# Comando para ejecutar el JAR
CMD ["java", "-jar", "target/colegio-0.0.1-SNAPSHOT.jar"]
