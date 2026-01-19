# Sistema de Gestión de Biblioteca (Microservicios)

Este proyecto es un sistema integral para la gestión de una biblioteca, desarrollado bajo una arquitectura de Microservicios utilizando Java 17 y Spring Boot 3.5.9. El sistema permite administrar el inventario de libros, la información de los usuarios y el flujo de préstamos de manera desacoplada y escalable.


## 🚀 Descripción del Sistema

El sistema se compone de tres microservicios principales y una puerta de enlace (API Gateway) que centraliza las comunicaciones:

API Gateway: Punto único de entrada que utiliza Spring Cloud Gateway para enrutar las peticiones a los servicios correspondientes mediante rutas unificadas.

Microservicio de Libros: Gestiona el catálogo de obras, autores y disponibilidad.

Microservicio de Usuarios: Administra el registro, perfiles y estados de los socios de la biblioteca.

Microservicio de Préstamos: Orquestador de la lógica de negocio para la salida y entrada de libros, vinculando libros con usuarios.


### 🛠️ Instalación y Configuración

Requisitos Previos

Java 17 (JDK)

Maven 3.9+

IDE (Recomendado: IntelliJ IDEA Community Edition)

1. Clonar el repositorio

git clone https://github.com/Fabiricu/sistema-biblioteca.git

cd sistema-biblioteca


2. Compilar el proyecto

Desde la carpeta raíz del proyecto, compila todos los módulos utilizando Maven:

mvn clean install


### 🏃 Ejecución de la Aplicación

Para que el sistema funcione correctamente, se recomienda seguir este orden de encendido:

Microservicios de Negocio: Ejecuta cada uno en terminales separadas.

Libros: cd microservicio-libros && mvn spring-boot:run (Puerto 8082)

Usuarios: cd microservicio-usuarios && mvn spring-boot:run (Puerto 8081)

Préstamos: cd microservicio-prestamos && mvn spring-boot:run (Puerto 8083)

API Gateway: Ejecuta el gateway al final.

cd api-gateway && mvn spring-boot:run (Puerto 8080)


### 📍 Disponibilidad y Endpoints

La aplicación está disponible a través del API Gateway en el puerto 8080. Se han configurado rutas amigables para el consumo externo:


Funcionalidad

URL Unificada (Gateway)


Método

Listar/Gestionar Libros

http://localhost:8080/biblioteca/libros

GET, POST, etc.

Gestión de Usuarios

http://localhost:8080/biblioteca/usuarios

GET, POST, etc.

Gestión de Préstamos

http://localhost:8080/biblioteca/prestamos

GET, POST, etc.


### 📊 Componentes Técnicos

Base de Datos

Cada microservicio utiliza su propia instancia de base de datos (H2 en memoria para desarrollo/MySQL Workbrench para producción), garantizando el principio de Database per Service.

Swagger (Documentación de API)

La documentación interactiva de cada microservicio está disponible en (ajustar puerto según servicio):

http://localhost:PORT/swagger-ui.html


### 📮 Colección de Postman

Para facilitar las pruebas, se incluye una colección de Postman con todas las peticiones configuradas para pasar por el Gateway.

Cómo usarla:

Localizamos el archivo : /docs/postman/MICROSERVICIO-LIBROS.postman_collection.json.
Localizamos el archivo : /docs/postman/MICROSERVICIO-PRESTAMOS.postman_collection.json.
Localizamos el archivo : /docs/postman/MICROSERVICIO-USUARIOS.postman_collection.json.

En Postman, haz clic en Import.

Arrastra el archivo JSON mencionado.

Verás la carpeta con las peticiones listas (Listar libros, Crear usuario, etc.).


### 📊 Componentes Técnicos

Base de Datos

Cada microservicio utiliza su propia instancia (H2 o MySQL Workbrench), siguiendo el patrón Database per Service.

Swagger (Documentación de API)

Disponible en cada servicio: http://localhost:PORT/swagger-ui.html


Ejecutar Tests

Para correr las pruebas unitarias y de integración de todo el sistema:

mvn test


Calidad del Código

El proyecto sigue las convenciones de código de Spring y Java. Se recomienda el uso de SonarQube o el plugin Checkstyle para verificar la calidad del código, manteniendo un enfoque en:

Código limpio (Clean Code)

Alta cobertura de tests

Desacoplamiento de componentes

Desarrollado con ❤️ por Fabiana.
