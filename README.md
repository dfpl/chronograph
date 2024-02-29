# Chronoweb

Chronoweb is an open-source web information system that analyzes temporal information diffusion with a unified RESTful web service. Users are able to create, retrieve, update, delete, and traverse graph elements such as vertices, edges, vertex events and edge events. Furthermore, the server allows subscribing a specific vertex source with an incremental computation program. The system is an open-source version of trilogy of ChronoGraph publications: 

- [Kairos, IEEE TKDE 2023](https://doi.ieeecomputersociety.org/10.1109/TKDE.2023.3347621)
- [Time-centric Engine, IEEE TKDE 2022](https://doi.org/10.1109/TKDE.2020.3005672)
- [ChronoGraph, IEEE TKDE 2020](https://doi.org/10.1109/TKDE.2019.2891565)

## Installation
### Environment
- [Open JDK 21](https://jdk.java.net/21/)
- [MongoDB 6.x](https://www.mongodb.com/try/download/community)

### Install Chronoweb as a user
1. Install JDK 21 and MongoDB
2. Set jdk bin folder as a system path
3. Download 'chronoweb.jar'
4. Run MongoDB
5. Run Chronoweb
```bash
java -jar chronoweb.jar [configuration.json]
```

### Install Chronoweb as a developer
1. Install JDK 21 and MongoDB
2. Install Eclipse IDE or IntelliJ IDE (Maven Project)
3. Clone and import the project
4. Run MongoDB
5. Run Server.java as a Java application

## OpenAPI 3.1 Specification
- [Chronoweb OpenAPI 3.1 specification](https://github.com/dfpl/chronograph/blob/main/openapi/chronoweb.json)
- [over Swagger-UI](https://app.swaggerhub.com/apis/BJW0829/Chronoweb/1.0.0)
