# Image d'execution legere (JRE uniquement) pour reduire la surface d'attaque
FROM eclipse-temurin:17-jre-alpine

# Mise a jour des paquets systeme (corrige les CVE connues de l'image de base)
RUN apk upgrade --no-cache

# Utilisateur non-root (bonne pratique de securite)
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY target/bibliotheque.jar app.jar
RUN chown app:app app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
