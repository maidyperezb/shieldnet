# ShieldNet

## Sistema de Gestión de Seguridad de Red con Integración de IA

**Fecha:** 18 de mayo de 2026  
**Institución Académica:** Fundacion Universitaria Tecnológica Comfenalco  
**Autores:**

- Juan Sebastián Arteaga Rodríguez
- Andrés Felipe Henríquez Castro
- Johan Estiven Giraldo Ramírez
- Maidy Stephanie Pérez Babilonia

<div class="page-break"></div>

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Stack y Ambiente Tecnológico](#stack-y-ambiente-tecnológico)
3. [Arquitectura por Capas](#arquitectura-por-capas)
4. [Explicación de Cada Capa](#explicación-de-cada-capa)
5. [Estructura del Proyecto Spring Boot](#estructura-del-proyecto-spring-boot)
6. [Vistas e Interfaz de Usuario](#vistas-e-interfaz-de-usuario)
7. [Integración con LLM (Groq API)](#integración-con-llm-groq-api)
8. [Diagrama de Modelo de Datos](#diagrama-de-modelo-de-datos)
9. [Repositorio GitHub](#repositorio-github)
10. [Referencias y Anexos](#referencias-y-anexos)

<div class="page-break"></div>

## Resumen Ejecutivo

**ShieldNet** es una aplicación web de gestión de seguridad de red construida con **Spring Boot 3.4.4** que integra un **chatbot inteligente basado en Groq API** (mediante Spring AI). La aplicación permite a usuarios autenticados:

- Monitorear eventos de seguridad en la red
- Consultar métricas de Calidad de Servicio (QoS)
- Gestionar dispositivos autorizados y no autorizados
- Dialogar con un asistente IA que analiza el contexto de eventos, métricas y dispositivos
- Ejecutar escaneos de red y análisis de puertos

La arquitectura sigue el patrón de **tres capas** (Controller → Service → Repository) con persistencia en **MySQL 8**, interfaz de usuario en **Thymeleaf + Bootstrap 5.3**, y seguridad mediante **Spring Security**.

<div class="page-break"></div>

## Stack y Ambiente Tecnológico

### Backend

| Componente          | Versión  | Propósito                             |
| ------------------- | -------- | ------------------------------------- |
| **Spring Boot**     | 3.4.4    | Framework principal de backend        |
| **Java (JDK)**      | 17       | Lenguaje de programación              |
| **Maven**           | 3.9.9+   | Gestor de dependencias y construcción |
| **Spring Security** | 6        | Autenticación y autorización          |
| **Spring Data JPA** | Bundled  | Acceso a datos con ORM                |
| **Hibernate**       | 6        | ORM para mapeo objeto-relacional      |
| **Spring AI**       | 1.0.0-M2 | Framework para integración con LLM    |

### Frontend

| Componente             | Versión | Propósito                      |
| ---------------------- | ------- | ------------------------------ |
| **Thymeleaf**          | 3.1+    | Template engine para vistas    |
| **Bootstrap**          | 5.3     | Framework CSS responsive       |
| **Vanilla JavaScript** | ES6+    | Lógica cliente (chat.js, etc.) |
| **marked.js**          | Latest  | Renderización de Markdown      |
| **DOMPurify**          | Latest  | Sanitización XSS               |

### Base de Datos

| Componente            | Versión | Propósito                    |
| --------------------- | ------- | ---------------------------- |
| **MySQL**             | 8.0+    | SGBD relacional (producción) |
| **MySQL Connector/J** | Latest  | Driver JDBC                  |

### Integración IA

| Componente      | Configuración                                              | Propósito                        |
| --------------- | ---------------------------------------------------------- | -------------------------------- |
| **Groq API**    | Base URL: `https://api.groq.com/openai`                    | Proveedor de LLM                 |
| **Modelo IA**   | `openai/gpt-oss-20b` (o `openai/gpt-oss-120b`)             | Generación de respuestas         |
| **Temperature** | 0.7                                                        | Grado de creatividad (0.0-1.0)   |
| **Max Tokens**  | 8192                                                       | Límite de respuesta por consulta |
| **Timeout**     | 30 segundos                                                | Tiempo máximo de espera          |

### Herramientas de Red y Telefonía

| Componente         | Versión   | Propósito                            |
| ------------------ | --------- | ------------------------------------ |
| **pcap4j**         | 1.8.2     | Captura y análisis de tráfico de red |
| **asterisk-java**  | 3.39.0    | Integración con Asterisk AMI (VoIP)  |
| **Windows Server** | (Externo) | Escaneo de ARP y gestión de red      |

### Variables de Entorno Requeridas

```bash
# LLM / Chat
GROQ_API_KEY=gsk_xxxxxxxxxxxxx
GROQ_API_BASE_URL=https://api.groq.com/openai
GROQ_CHAT_MODEL=openai/gpt-oss-20b

# Base de Datos
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=shieldnet
MYSQL_USERNAME=root
MYSQL_PASSWORD=

# Asterisk (Telefonía)
# En Docker: docker compose up -d --build
ASTERISK_HOST=localhost
ASTERISK_PORT=5038
ASTERISK_USERNAME=admin
ASTERISK_PASSWORD=Admin123
ASTERISK_CDR_PATH=docker/asterisk/data/log/cdr-csv/Master.csv
ASTERISK_EXTERNAL_ADDRESS=127.0.0.1

# Servidor
SERVER_PORT=8080
```

### Entorno de Ejecución

**Desarrollo:**

```bash
mvn spring-boot:run
```

**Producción:**

```bash
java -jar target/security-demo-0.0.1-SNAPSHOT.jar
# Con variables de entorno definidas
```

<div class="page-break"></div>

**Requisitos mínimos:**

- Java 17+ (OpenJDK o Oracle)
- MySQL 8.0+ o MariaDB 10.4+
- 1024 MB RAM mínimo
- Conexión a Internet (para Groq API)

<div class="page-break"></div>

## Arquitectura por Capas

ShieldNet implementa una arquitectura de **5 capas** (tres niveles):

```textplain
┌─────────────────────────────────────────────────┐
│ CAPA PRESENTACIÓN (Vista)                       │
│ • Thymeleaf templates (HTML)                    │
│ • Bootstrap 5.3 (CSS/UI)                        │
│ • Vanilla JavaScript (lógica cliente)           │
└────────────────────┬────────────────────────────┘
                     │ HTTP GET/POST
                     ▼
┌─────────────────────────────────────────────────┐
│ CAPA LÓGICA (Controllers)                       │
│ • @RestController + @Controller                 │
│ • 11 controladores especializados               │
│ • Gestión de requests y responses               │
└────────────────────┬────────────────────────────┘
                     │ Invocan
                     ▼
┌─────────────────────────────────────────────────┐
│ CAPA NEGOCIO (Services)                         │
│ • @Service con lógica de negocio                │
│ • 15 servicios especializados                   │
│ • Transacciones y validaciones                  │
└────────────────────┬────────────────────────────┘
                     │ Query/Persist
                     ▼
┌─────────────────────────────────────────────────┐
│ CAPA DATOS (Repositories)                       │
│ • Spring Data JPA repositories                  │
│ • 6 repositorios + consultas personalizadas     │
│ • Mapeo a entidades (Entities)                  │
└────────────────────┬────────────────────────────┘
                     │ SQL/Hibernate
                     ▼
┌─────────────────────────────────────────────────┐
│ CAPA PERSISTENCIA (Base de Datos)               │
│ • MySQL 8 / MariaDB                             │
│ • 6 tablas relacionales + índices               │
│ • Relaciones 1:N y contraints                   │
└─────────────────────────────────────────────────┘
```

<div class="page-break"></div>

### Flujo de Extremo a Extremo

Un request HTTP típico sigue este flujo:

```textplain
Cliente (Navegador)
    ↓ POST /api/chat/message?query="..."
Controller (ChatController.handleMessage)
    ↓ Extrae query y usuario autenticado
Service (ChatService.procesarPrompt)
    ├─→ ContextBuilder: construye contexto de BD
    ├─→ ChatAIService: invoca Groq API con tools
    └─→ ConversacionRepository.save(): persiste respuesta
Repository (JPA Query)
    ↓ INSERT INTO conversaciones (usuario_id, prompt, respuesta, created_at)
Base de Datos (MySQL)
    ↓ Ejecuta SQL, retorna affected rows
Response: 200 OK + JSON con respuesta en stream SSE
    ↓
Navegador: renderiza markdown + actualiza historial
```

<div class="page-break"></div>


## Explicación de Cada Capa

### Capa Presentación (Vistas)

**Responsabilidad:** Renderizar interfaz de usuario e interactuar con el usuario.

**Tecnologías:** Thymeleaf (template engine), Bootstrap 5.3 (CSS), JavaScript vanilla.

**Componentes principales:**

- `home.html` - Página de inicio con información general
- `login.html` - Formulario de autenticación
- `admin.html` - Dashboard administrativo con métricas
- `user.html` - Dashboard de usuario con historial
- `chat-widget.html` - Fragmento reutilizable del chat flotante
- `static/js/chat.js` - Lógica de cliente del chat (SSE listener, envío de mensajes)

**Características de seguridad:**

- Validación de sesión en servidor (Spring Security)
- Protección CSRF mediante tokens
- Sanitización de entrada con DOMPurify en frontend

---

### Capa Lógica (Controllers)

**Responsabilidad:** Recibir requests HTTP, validar entrada, invocar servicios y retornar respuestas.

**Patrón:** @RestController (JSON APIs) + @Controller (vistas HTML).

**11 Controladores implementados:**

1. **AuthController** - Gestión de autenticación y vistas MVC
   - POST `/registrar` - Registro público de usuarios
   - GET `/login` - Página de login
   - GET `/admin`, `/user`, `/home` - Vistas protegidas

2. **ChatController** - APIs para chatbot IA
   - POST `/api/chat/message` - Procesar pregunta con streaming SSE
   - GET `/api/chat/history` - Obtener historial paginado
   - DELETE `/api/chat/history` - Limpiar conversaciones (soft delete)
   - GET `/api/chat/count` - Estadísticas de conversaciones

3. **UsuarioController** - Gestión de usuarios
   - GET/POST `/api/usuarios` - CRUD de usuarios
   - GET `/api/usuarios/{id}` - Detalle de usuario

4. **DispositivoController** - Gestión de dispositivos
   - GET/POST `/api/dispositivos` - CRUD de dispositivos
   - GET `/api/dispositivos/{id}` - Detalle

5. **EventoController** - Registro y consulta de eventos
   - GET/POST `/api/eventos` - CRUD de eventos

6. **QoSController** - Medición de Calidad de Servicio
   - POST `/api/network/qos/measure` - Ejecutar medición
   - GET `/api/network/qos/latest` - Últimas métricas

7. **NetworkController** - Operaciones de red
   - POST `/api/ping` - Ejecutar ping
   - POST `/api/scan` - Escaneo de puertos

8. **AsteriskController** - Integración VoIP
   - GET `/api/voip/status` - Estado de Asterisk
   - GET `/api/voip/extensions` - Extensiones disponibles

9. **MacAutorizadaController** - Gestión de direcciones MAC autorizadas
   - GET/POST/DELETE `/api/macs` - CRUD de MACs

10. **NetworkTrafficController** - Análisis de tráfico
    - GET `/api/network/admin/network-usage` - Uso de ancho de banda

11. **TemporalController** - Funcionalidades auxiliares
    - GET `/admin/interfaces` - Interfaces de red

**Validación y seguridad:**

- Todos los endpoints excepto `/login`, `/registrar`, `/home` requieren autenticación
- Autorización con roles: `@hasRole("ADMIN")` o `@hasRole("USER")`
- Validación de parámetros en cada endpoint
- Manejo de excepciones con ResponseEntity

---

### Capa Negocio (Services)

**Responsabilidad:** Implementar lógica de negocio, transacciones, validaciones y orquestación.

**Patrón:** @Service, @Transactional, inyección de dependencias.

**15 Servicios implementados:**

**Servicios de Entidades Principales:**

1. **UserService** - Gestión de usuarios
   - `save(usuarioDTO)` - Crear/actualizar usuario
   - `registrarUsuarioPublico()` - Autoregistro
   - `findByUsername(username)` - Búsqueda
   - Encriptación de contraseña con BCrypt

2. **DispositivoService** - Gestión de dispositivos
   - `saveDispositivo(dispositivo)` - Crear/actualizar
   - `findByIp(ip)` - Búsqueda por IP
   - Validación de estado de autorización

3. **EventoService** - Registro de eventos
   - `registrarEvento(ip, descripcion, usuario)` - Crear evento con timestamp
   - Asociación automática de usuario

4. **MacAutorizadaService** - Control de acceso MAC
   - `agregarMac(mac, descripcion)` - Agregar MAC a whitelist
   - `esMacAutorizada(mac)` - Validar contra whitelist

**Servicios de Monitoreo y Red:**

5. **QoSService** - Medición de calidad de servicio

- `measureQoS(targetIp, packetCount)` - Ejecutar ping
- Parsing de salida del SO
- Cálculo de latencia, jitter, packet loss, MOS score
- Persistencia de métricas

6. **NetworkService** - Operaciones de red
   - `executePing(ip)` - Ping a host
   - `scanPorts(ip, startPort, endPort)` - Escaneo de puertos
   - Registro automático de eventos

7. **ArpScannerService** - Scanner ARP automático
   - Descubrimiento de dispositivos en la red
   - Integración con Windows Server

8. **NetworkTrafficService** - Captura de tráfico
   - `capturePackets()` - Usar pcap4j para análisis
   - Estadísticas de ancho de banda

9. **PortSecurityAnalyzer** - Análisis de seguridad de puertos
   - Identificación de puertos abiertos/riesgosos
   - Recomendaciones de seguridad

10. **AsteriskAMIService** - Integración VoIP
    - Conexión a Asterisk AMI
    - Consulta de estado de extensiones
    - Monitoreo de llamadas

**Servicios de Chat e IA:**

11. **ChatService** - Orquestación de chat - `procesarPrompt(query, usuarioId)` - Stream SSE de respuesta - `obtenerHistorial(usuarioId)` - Historial de conversaciones - `limpiarHistorial(usuarioId)` - Soft delete de conversaciones - `limpiarConversacionesAntiguas(diasRetention)` - Purga de datos antiguos - Persistencia de conversaciones en BD

12. **ContextBuilder** - Construcción de contexto para IA
    - `buildFullContext(usuario)` - Consolida todos los contextos
    - `buildEventContext()` - Últimos N eventos (7 días default)
    - `buildQoSContext()` - Métricas de red con promedios
    - `buildDeviceContext()` - Dispositivos autorizados/no-autorizados
    - `buildSystemPrompt()` - Carga prompt del chatbot desde archivo

13. **ChatAIService** - Integración con Groq API
    - `getChatClient()` - Bean de ChatClient configurado
    - Define **5 tools de Function Calling:**
      - `queryEventHistory(startDate, endDate)` - Consulta eventos en rango
      - `queryQoSMetrics(days, protocolType)` - Extrae métricas QoS
      - `listAuthorizedDevices()` - Lista dispositivos por estado
      - `executeNetworkScan(type, subnet)` - Escaneo ARP/puertos
      - `generateMonitoringReport(days)` - Reporte completo
    - Cada tool retorna String formateado para visualización

14. **CustomUserDetailsService** - Autenticación Spring
    - `loadUserByUsername(username)` - Carga usuario para autenticación
    - Retorna UserDetails con roles

15. **QoSScheduledService** - Tareas programadas
    - `@Scheduled` para mediciones automáticas periódicas
    - Limpieza de datos viejos

---

### Capa Datos (Repositories)

**Responsabilidad:** Acceso a datos mediante Spring Data JPA, con consultas personalizadas usando @Query.

**Patrón:** Interfaces que extienden JpaRepository<T, Long>.

**6 Repositorios implementados:**

1. **UsuarioRepository**
   - `findByUsername(username)` - Búsqueda por usuario
   - `findByEmail(email)` - Búsqueda por email
   - `existsByUsername(username)` - Validación de existencia

2. **ConversacionRepository** (Nuevo para chat)
   - `findByUsuarioIdOrderByCreatedAtDesc(usuarioId)` - Historial activo
   - `findRecentConversations(usuarioId, limit)` - Últimas N
   - `softDeleteByUsuarioId(usuarioId)` - Soft delete (marca deleted_at)
   - `countByUsuarioId(usuarioId)` - Estadísticas

3. **DispositivoRepository**
   - `findByIp(ip)` - Búsqueda por IP
   - `findByMac(mac)` - Búsqueda por dirección MAC
   - `findByEstadoAutorizacion(estado)` - Filtro por estado

4. **EventoRepository**
   - `findByUsuarioId(usuarioId)` - Eventos de un usuario
   - `findByFechaBetween(startDate, endDate)` - Rango de fechas
   - Índices en usuario_id y fecha para performance

5. **MacAutorizadaRepository**
   - `existsByMac(mac)` - Validación de whitelist
   - `findByMac(mac)` - Búsqueda exacta

6. **QoSMetricsRepository**
   - `findRecentMetrics(since)` - Métricas posteriores a fecha
   - `getAverageMosScore()` - Agregación de MOS

**Características:**

- Todas usan @Query personalizado donde es necesario
- Soft delete mediante `deletedAt IS NULL`
- Índices en columnas clave para queries frecuentes
- @Transactional @Modifying para operaciones de escritura

---

### Capa Persistencia (Entidades y Base de Datos)

**Responsabilidad:** Mapeo objeto-relacional y almacenamiento de datos.

**6 Tablas principales:**

#### 1. **usuarios**

```textplain
id (PK, BIGINT AUTO_INCREMENT)
username (VARCHAR 255, UNIQUE)
password (VARCHAR 255, BCrypt hash)
nombre (VARCHAR 255)
apellido (VARCHAR 255)
email (VARCHAR 255, UNIQUE)
ultimo_acceso (DATETIME)
```

**Relaciones:** 1:N → roles, eventos, conversaciones

#### 2. **roles** (ElementCollection)

```textplain
id (PK)
usuario_id (FK → usuarios.id, CASCADE DELETE)
rol (VARCHAR 255) - "ROLE_ADMIN", "ROLE_USER"
```

#### 3. **conversaciones** (Nueva)

```textplain
id (PK, BIGINT AUTO_INCREMENT)
usuario_id (FK → usuarios.id, CASCADE DELETE)
prompt (LONGTEXT)
respuesta (LONGTEXT)
tool_calls (LONGTEXT, nullable) - JSON con invocaciones intermedias
tokens_used (INT, nullable)
response_time_ms (BIGINT, nullable)
created_at (DATETIME DEFAULT CURRENT_TIMESTAMP)
deleted_at (DATETIME, nullable) - Soft delete
```

**Índices:** `idx_usuario_created (usuario_id, created_at DESC)`

#### 4. **dispositivos**

```textplain
id (PK)
ip (VARCHAR 255, UNIQUE)
mac (VARCHAR 17)
estado_autorizacion (VARCHAR 20) - Enum: AUTORIZADO, NO_AUTORIZADO, DESCONOCIDO
ultima_conexion (DATETIME)
```

#### 5. **eventos**

```textplain
id (PK)
usuario_id (FK → usuarios.id, CASCADE DELETE)
ip (VARCHAR 255)
fecha (DATE)
hora (TIME)
descripcion (VARCHAR 255)
```

**Índices:** usuario_id, fecha

#### 6. **macs_autorizadas**

```textplain
id (PK)
mac (VARCHAR 17, UNIQUE)
descripcion (VARCHAR 255)
fecha_registro (DATETIME)
```

#### 7. **qos_metrics**

```textplain
id (PK)
target_ip (VARCHAR 255)
timestamp (DATETIME)
latency_ms (DOUBLE)
jitter_ms (DOUBLE)
packet_loss_percent (DOUBLE)
packets_sent, packets_received, packets_lost (INT)
mos_score (DOUBLE) - Mean Opinion Score (1.0-5.0)
rtt_avg_ms, rtt_min_ms, rtt_max_ms (DOUBLE)
protocol_type (VARCHAR 255) - ICMP, TCP, UDP, VOIP
quality_status (VARCHAR 255) - EXCELLENT, GOOD, FAIR, POOR, BAD
during_active_call (TINYINT BOOLEAN)
```

<div class="page-break"></div>

## Estructura del Proyecto Spring Boot

```textplain
shieldnet/
│
├── pom.xml                          # Configuración Maven + dependencias
├── mvnw, mvnw.cmd                   # Maven Wrapper
│
├── src/
│   ├── main/
│   │   ├── java/com/example/security/
│   │   │   ├── SecurityDemoApplication.java       # @SpringBootApplication entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java            # Spring Security beans y autorización
│   │   │   │
│   │   │   ├── controller/                        # 11 Controllers
│   │   │   │   ├── ChatController.java            # POST /api/chat/message, GET history, DELETE clear
│   │   │   │   ├── AuthController.java            # POST /registrar, GET /login, /admin, /user
│   │   │   │   ├── UsuarioController.java
│   │   │   │   ├── DispositivoController.java
│   │   │   │   ├── EventoController.java
│   │   │   │   ├── QoSController.java
│   │   │   │   ├── NetworkController.java
│   │   │   │   ├── AsteriskController.java
│   │   │   │   ├── MacAutorizadaController.java
│   │   │   │   ├── NetworkTrafficController.java
│   │   │   │   └── TemporalController.java
│   │   │   │
│   │   │   ├── servicios/                         # 15 Services
│   │   │   │   ├── ChatService.java               # Orquestación chat
│   │   │   │   ├── ContextBuilder.java            # Construcción contexto IA
│   │   │   │   ├── ChatAIService.java             # Integración Groq + tools
│   │   │   │   ├── UserService.java
│   │   │   │   ├── DispositivoService.java
│   │   │   │   ├── EventoService.java
│   │   │   │   ├── QoSService.java
│   │   │   │   ├── NetworkService.java
│   │   │   │   ├── MacAutorizadaService.java
│   │   │   │   ├── CustomUserDetailsService.java  # Authentication
│   │   │   │   ├── AsteriskAMIService.java
│   │   │   │   ├── ArpScannerService.java
│   │   │   │   ├── NetworkTrafficService.java
│   │   │   │   ├── PortSecurityAnalyzer.java
│   │   │   │   └── QoSScheduledService.java
│   │   │   │
```

<div class="page-break"></div>

```textplain
│   │   │   ├── repositorio/                       # 6 Repositories
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   ├── ConversacionRepository.java
│   │   │   │   ├── DispositivoRepository.java
│   │   │   │   ├── EventoRepository.java
│   │   │   │   ├── MacAutorizadaRepository.java
│   │   │   │   └── QoSMetricsRepository.java
│   │   │   │
│   │   │   ├── modelo/                            # Entities + Enums + DTOs
│   │   │   │   ├── Usuario.java                   # @Entity
│   │   │   │   ├── Conversacion.java              # @Entity
│   │   │   │   ├── Dispositivo.java               # @Entity
│   │   │   │   ├── Evento.java                    # @Entity
│   │   │   │   ├── MacAutorizada.java             # @Entity
│   │   │   │   ├── QoSMetrics.java                # @Entity
│   │   │   │   ├── EstadoAutorizacion.java        # Enum
│   │   │   │   ├── Protocolo.java                 # Enum
│   │   │   │   ├── chat/                          # Modelos para chat/tools
│   │   │   │   │   ├── EventHistoryRequest.java
│   │   │   │   │   ├── QoSMetricsRequest.java
│   │   │   │   │   ├── NetworkScanRequest.java
│   │   │   │   │   └── ReportRequest.java
│   │   │   │   ├── PingRequest.java
│   │   │   │   ├── PingResponse.java
│   │   │   │   ├── PortScanRequest.java
│   │   │   │   └── PortScanResponse.java
│   │   │   │
│   │   │   ├── dto/                               # Data Transfer Objects
│   │   │   │   ├── ConversacionDTO.java
│   │   │   │   ├── UsuarioDTO.java
│   │   │   │   ├── DispositivoDTO.java
│   │   │   │   ├── EventoDTO.java
│   │   │   │   └── QoSData.java
│   │   │   │
│   │   │   └── generador/
│   │   │       └── PasswordGenerator.java         # Utilería
│   │   │
│   │   └── resources/
│   │       ├── application.properties             # Config base (BD, Asterisk, logging)
│   │       ├── application-chat.properties        # Config Groq + chat service
│   │       │
│   │       ├── db/
│   │       │   └── shieldnet.sql                  # SQL dump con schema e inserts
│   │       │
│   │       ├── prompts/
│   │       │   └── system-prompt.txt              # System prompt del chatbot
│   │       │
```

<div class="page-break"></div>

```textplain
│   │       ├── templates/                         # Thymeleaf templates
│   │       │   ├── home.html                      # Landing page
│   │       │   ├── login.html                     # Formulario login
│   │       │   ├── registrar.html                 # Formulario registro
│   │       │   ├── admin.html                     # Dashboard admin
│   │       │   ├── user.html                      # Dashboard user
│   │       │   └── fragments/
│   │       │       └── chat-widget.html           # Chat flotante (reutilizable)
│   │       │
│   │       └── static/
│   │           ├── css/
│   │           │   └── [estilos personalizados]
│   │           ├── img/
│   │           │   └── [imágenes/iconos]
│   │           └── js/
│   │               ├── chat.js                    # Lógica cliente chat (SSE, fetch)
│   │               ├── marked.min.js              # Markdown renderer
│   │               └── purify.min.js              # XSS sanitizer
│   │
│   └── test/
│       └── java/com/example/security/
│           └── SecurityDemoApplicationTests.java  # Test suite básico
│
├── target/                          # Artefactos compilados
│   ├── classes/                     # .class compilados
│   ├── security-demo-0.0.1-SNAPSHOT.jar # Fat JAR ejecutable
│   └── generated-sources/           # Código generado
│
└── docs/
    ├── github-link.txt              # Enlace al repositorio
    └── DOCUMENTO_TECNICO_DESARROLLO_WEB_AVANZADO.md  # Este archivo
```

### Paquetes Java - Responsabilidades

- **com.example.security** - Paquete raíz de la aplicación
- **com.example.security.config** - Configuración de Spring (Security, Beans)
- **com.example.security.controller** - 11 Controllers REST + MVC
- **com.example.security.servicios** - 15 Services con lógica de negocio
- **com.example.security.repositorio** - 6 JPA Repositories
- **com.example.security.modelo** - Entities (@Entity) + Enums
- **com.example.security.dto** - Data Transfer Objects (DTOs)
- **com.example.security.generador** - Utilidades (PasswordGenerator)

<div class="page-break"></div>

## Vistas e Interfaz de Usuario

### Páginas Principales (Thymeleaf)

La aplicación implementa una interfaz web responsiva usando **Thymeleaf** como template engine y **Bootstrap 5.3** para estilos CSS. Las páginas principales son:

#### 1. **home.html** - Página de Inicio

- Landing page pública (sin autenticación requerida)
- Header con logo "ShieldNet" y botón de Login
- Sección hero con descripción de la aplicación
- Listado de tecnologías utilizadas (Spring Boot, Java, MySQL, Bootstrap, Groq AI)
- Botón de "Registrarse" para nuevos usuarios
- Footer con información de contacto
- Fondo: Gradiente linear (azul claro a gris)

**Código:** `src/main/resources/templates/home.html`

#### 2. **login.html** - Autenticación

- Formulario de login (POST `/login`)
- Campos: username, password
- Link a "/registrar" para nuevos usuarios
- Validación en cliente (HTML5) y servidor (Spring Security)
- Redirección post-login a "/" (homepage autenticada)

#### 3. **registrar.html** - Autoregistro de Usuarios

- Formulario de registro público (POST `/registrar`)
- Campos: username, password, nombre, apellido, email
- Validación: username y email únicos, password ≥ 8 caracteres
- BCrypt hashing automático de contraseña
- Asignación automática de rol: ROLE_USER

#### 4. **admin.html** - Panel Administrativo

- Dashboard exclusivo para usuarios con rol ROLE_ADMIN
- Header con gradiente (tonos azul/gris oscuro)
- Sidebar de navegación con opciones:
  - Ver usuarios
  - Gestionar dispositivos
  - Eventos de seguridad
  - Métricas QoS
  - Configuración
- Área principal con:
  - Tarjetas de estadísticas (cards)
  - Gráficos con Chart.js (líneas, barras)
  - Tablas interactivas con datos de BD
- Chat widget flotante integrado (ver sección siguiente)

**Nota:** Incluye el chat widget flotante via fragment `chat-widget`

#### 5. **user.html** - Panel de Usuario

- Dashboard personalizado para usuarios con rol ROLE_USER
- Acceso limitado a:
  - Datos de dispositivos autorizados
  - Historial de eventos propios (filtrado por usuario_id)
  - Métricas QoS (consulta)
  - Chat widget para consultas inteligentes
- Interfaz simplificada respecto a admin.html

### Chat Widget Flotante

**Ubicación:** `src/main/resources/templates/fragments/chat-widget.html`

**Características:**

- Componente Thymeleaf reutilizable (`th:fragment="chat-widget"`)
- Posición: Fixed bottom-right (z-index: 999999)
- Estado: Minimizado por defecto (botón flotante de 60×60px)
- Al hacer click: se expande a ventana modal (400×600px)

**Elementos visuales:**

- **Botón flotante:** Ícono de chat, fondo azul (#007bff), shadow
- **Ventana expandida:**
  - Header azul con título "Asistente de IA" y botón cerrar
  - Área de mensajes (max-height 400px, scrollable)
  - Input textarea con placeholder descriptivo
  - Botones: "Enviar" (azul), "Limpiar historial" (rojo)
  - Status área (muestra "Procesando...", "Completado", etc.)

**Estilos:**

- Animación `slideIn` (0.3s ease) al abrir ventana
- Hover effects en botones
- Renderización de markdown en respuestas (via marked.js)
- Sanitización XSS (via DOMPurify)

**Integración JavaScript:**

- Script `src/main/resources/static/js/chat.js` se carga automáticamente
- Clase `ChatWidget` gestiona:
  - Envío de mensaje: POST `/api/chat/message`
  - Carga de historial: GET `/api/chat/history`
  - Limpieza: DELETE `/api/chat/history`

### JavaScript Client (chat.js)

**Responsabilidades:**

1. **Inicialización:** `ChatWidget` constructor genera sessionId único
2. **Event listeners:** Botones (toggle, close, send, clear) + Enter key
3. **SSE Streaming:** Fetch a `/api/chat/message` con parámetros query + sessionId
4. **Parser SSE:** Lee eventos del servidor (type: "message", "error", "complete")
5. **Renderizado:** Markdown (marked.js) + Sanitización (DOMPurify)
6. **Historial:** Carga conversaciones previas en orden inverso

**Flujo de envío de mensaje:**

```javascript
1. Usuario escribe query + pulsa Enviar
2. ChatWidget.sendMessage() valida entrada
3. Fetch POST /api/chat/message?query=...&sessionId=...
4. Response: ReadableStream (Server-Sent Events)
5. Parser SSE: event.type + JSON.parse(data)
6. Si type="message": acumula content y renderiza markdown
7. Si type="complete": finaliza y guarda en historial local
8. Si type="error": muestra alerta roja
```

**Métodos clave:**

- `sendMessage()` - Valida, envía, procesa streaming SSE
- `loadHistory()` - GET /api/chat/history, invierte orden, muestra últimas 20
- `clearHistory()` - DELETE /api/chat/history con confirmación
- `processSseChunk(line, callback)` - Parser robusto de líneas SSE
- `addMessage(content, role)` - Inserta div con estilos según role (user/assistant/system)
- `updateMessage(messageId, content)` - Actualiza HTML de mensaje en tiempo real

<div class="page-break"></div>

## Integración con LLM (Groq API)

### Configuración de Spring AI

**Archivo:** `src/main/resources/application-chat.properties`

**Proveedor:** Groq API (OpenAI-compatible endpoint)

```properties
# Credenciales
spring.ai.openai.api-key=${GROQ_API_KEY:}
spring.ai.openai.base-url=https://api.groq.com/openai

# Modelo y parámetros
spring.ai.openai.chat.options.model=llama-3.1-8b-instant
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.top-p=0.9
spring.ai.openai.chat.options.max-tokens=2048
spring.ai.openai.chat.options.timeout=30s

# Configuración del servicio de chat
chat.system.prompt.template=classpath:prompts/system-prompt.txt
chat.max-history=20
chat.context-days=7
chat.enable-function-calling=false
```

### Endpoints REST del Chat

#### POST `/api/chat/message`

- **Parámetros:** query (String), sessionId (String, opcional)
- **Response:** `Flux<ServerSentEvent<String>>` (HTTP 200 con `text/event-stream`)
- **Formato respuesta:**

  ```json
  event: message
  id: uuid
  data: {"type":"message","content":"..."}

  event: complete
  data: {"type":"complete","content":"OK"}
  ```

- **Seguridad:** Requiere autenticación (usuario en Principal)
- **Validación:** query no puede estar vacío

#### GET `/api/chat/history`

- **Parámetros:** limit (Integer, default=20)
- **Response:** `ResponseEntity<List<ConversacionDTO>>` (200 OK)
- **Descripción:** Retorna últimas N conversaciones activas (deletedAt IS NULL)
- **Orden:** DESC por createdAt (más recientes primero)

#### DELETE `/api/chat/history`

- **Response:** 204 No Content
- **Descripción:** Soft delete de conversaciones (marca deletedAt = NOW())
- **Nota:** Requiere rol ROLE_ADMIN (pendiente validar en código)

#### GET `/api/chat/count`

- **Response:** `{"count": 42}` (JSON)
- **Descripción:** Estadísticas de conversaciones del usuario

### ChatService - Orquestación

**Archivo:** `src/main/java/com/example/security/servicios/ChatService.java`

**Método principal:** `procesarPrompt(query, usuarioId) → Flux<String>`

**Flujo:**

1. **Validación:** Obtiene usuario desde UsuarioRepository
2. **Contexto:** Invoca ContextBuilder.buildFullContext(usuario)
3. **System Prompt:** Carga desde `system-prompt.txt`
4. **ChatClient:**
   - `chatClient.prompt()`
   - `.system(systemPrompt)`
   - `.user(context + "\n\nPregunta: " + query)`
   - `.stream().content()` → Flux<String>
5. **Streaming:** Acumula chunks en StringBuilder
6. **Persistencia:** Al completar, invoca `saveConversation(usuario, query, respuesta, responseTimeMs)`
7. **Retorno:** Flux con chunks individuales para SSE

**Métodos adicionales:**

- `obtenerHistorial(usuarioId)` - Lista de Conversacion DESC
- `obtenerUltimasConversaciones(usuarioId, limit)` - Paginación
- `limpiarHistorial(usuarioId)` - Soft delete (@Modifying @Query)
- `limpiarConversacionesAntiguas(diasRetention)` - Limpieza automática

### ContextBuilder - Construcción de Contexto

**Archivo:** `src/main/java/com/example/security/servicios/ContextBuilder.java`

**Método público:** `buildFullContext(Usuario usuario, int maxChars) → String`

**Contenido del contexto:**

1. **Schema Description** - Tablas disponibles (usuarios, eventos, dispositivos, qos_metrics)
2. **Eventos Históricos**
   - Rango: Últimos N días (configurable, default 7)
   - Campos: fecha, hora, IP, descripción, usuario
   - Límite: 200 máximo, muestra 10 en contexto
3. **Métricas QoS**
   - Promedios: latencia (ms), jitter (ms), pérdida de paquetes (%), MOS score
   - Alertas automáticas si:
     - latencia > 100ms
     - packet_loss > 1%
     - MOS score < 3.5
4. **Dispositivos**
   - Conteos: autorizados vs no-autorizados
   - Listado de dispositivos no autorizados (con IP/MAC)
   - Recomendación de investigación

**Parámetros configurables (application-chat.properties):**

- `chat.context-days=7` - Ventana temporal de contexto
- `chat.max-context-chars=12000` - Límite de caracteres a enviar
- `chat.max-events=200` - Máximo eventos a consultar
- `chat.max-event-samples=10` - Eventos mostrados en contexto
- `chat.max-device-details=20` - Dispositivos no auth a listar

### ChatAIService - Integración con Groq

**Archivo:** `src/main/java/com/example/security/servicios/ChatAIService.java`

**Bean principal:** `getChatClient() → ChatClient`

```java
@Bean
public ChatClient getChatClient() {
    return ChatClient.builder(chatModel)
        .defaultFunctions("queryEventHistory", "queryQoSMetrics",
                         "listAuthorizedDevices", "executeNetworkScan",
                         "generateMonitoringReport")
        .build();
}
```

**5 Tools (Function Calling) definidos:**

1. **queryEventHistory(EventHistoryRequest)**
   - Parámetros: startDate, endDate, maxResults
   - Retorna: String con lista de eventos formateados
   - Descripción: "Consulta el historial de eventos de seguridad en rango de fechas"

2. **queryQoSMetrics(QoSMetricsRequest)**
   - Parámetros: days, protocolType
   - Retorna: String con métricas agregadas y análisis
   - Descripción: "Consulta métricas de QoS (latencia, jitter, packet loss, MOS)"

3. **listAuthorizedDevices()**
   - Sin parámetros
   - Retorna: String con dispositivos separados por estado
   - Descripción: "Lista dispositivos autorizados y no autorizados"

4. **executeNetworkScan(NetworkScanRequest)**
   - Parámetros: type (arp|port), subnet
   - Retorna: String con resultado del escaneo
   - Descripción: "Ejecuta escaneo de red para descubrir dispositivos"

5. **generateMonitoringReport(ReportRequest)**
   - Parámetros: days
   - Retorna: String con reporte consolidado
   - Descripción: "Genera reporte completo de eventos, QoS y dispositivos"

### System Prompt del Chatbot

**Archivo:** `src/main/resources/prompts/system-prompt.txt`

**Identidad:** "ShieldNet AI" - Experto en ciberseguridad e infraestructura de red

**Capacidades técnicas:**

- Análisis de eventos de seguridad
- Consulta de métricas QoS y estado de red
- Gestión de dispositivos autorizados/no-autorizados
- Interpretación de configuraciones Asterisk (VoIP)
- Escaneo ARP y análisis de puertos

**Reglas de respuesta:**

1. Idioma: Español, tono profesional y técnico
2. Precisión: Datos verificables sobre suposiciones
3. Seguridad: Alertas para IPs no autorizadas y puertos críticos abiertos
4. Concisión: Directo al grano sin introducciones
5. Formato: Tablas para métricas, bloques de código para logs/configs

**Procedimiento de diagnóstico:**

- Lentitud → `queryQoSMetrics()`
- Dispositivos desconocidos → `executeNetworkScan()`
- Actividad reciente → `queryEventHistory()`

**Restricciones:**

- No revela contraseñas del sistema
- No realiza cambios en BD sin autorización explícita

### Persistencia de Conversaciones

**Tabla:** `conversaciones`

**Entidad:** [src/main/java/com/example/security/modelo/Conversacion.java](src/main/java/com/example/security/modelo/Conversacion.java)

**Campos:**

```java
@Id @GeneratedValue(IDENTITY) Long id;
@ManyToOne Usuario usuario;
@Column(LONGTEXT) String prompt;
@Column(LONGTEXT) String respuesta;
@Column(LONGTEXT) String toolCalls;  // JSON con llamadas intermedias
Integer tokensUsed;
Long responseTimeMs;
@CreationTimestamp LocalDateTime createdAt;
@Column LocalDateTime deletedAt;  // Soft delete
```

**Índices:** `idx_usuario_created (usuario_id, created_at DESC)`

**Operaciones:**

- **Insert:** ChatService.saveConversation() tras completar respuesta
- **Select:** ConversacionRepository.findByUsuarioIdOrderByCreatedAtDesc()
- **Soft Delete:** ConversacionRepository.softDeleteByUsuarioId() (marca deletedAt)
- **Purga:** ChatService.limpiarConversacionesAntiguas(diasRetention) con @Scheduled

<div class="page-break"></div>

## Diagrama de Modelo de Datos

### Diagrama Entidad-Relación (E-R)

```textplain
┌─────────────────────────────────────────────────────────┐
│                      USUARIOS                           │
├─────────────────────────────────────────────────────────┤
│ PK: id (BIGINT)                                         │
│ username (VARCHAR 255, UNIQUE, NOT NULL)                │
│ password (VARCHAR 255, NOT NULL) - BCrypt hash          │
│ nombre (VARCHAR 255, NOT NULL)                          │
│ apellido (VARCHAR 255, NOT NULL)                        │
│ email (VARCHAR 255, UNIQUE, NOT NULL)                   │
│ ultimo_acceso (DATETIME, nullable)                      │
│                                                          │
│ FK_ROLES: 1:N → ROLES (usuario_id)                      │
│ FK_EVENTOS: 1:N → EVENTOS (usuario_id)                  │
│ FK_CONVERSACIONES: 1:N → CONVERSACIONES (usuario_id)    │
│                                                          │
│ Índices: UK_username, UK_email                          │
└────────────────────┬────────────────────────────────────┘
                     │ 1:N
                     ├─────────────┬─────────────┬──────────────┐
                     │             │             │              │
        ┌────────────▼──────┐  ┌───▼─────────┐  ┌──▼────────────────┐
        │      ROLES        │  │   EVENTOS   │  │ CONVERSACIONES    │
        ├───────────────────┤  ├─────────────┤  ├───────────────────┤
        │ PK: id            │  │ PK: id      │  │ PK: id            │
        │ usuario_id (FK)   │  │ usuario_id  │  │ usuario_id (FK)   │
        │ rol (VARCHAR)     │  │   (FK)      │  │ prompt (LONGTEXT) │
        │ - ROLE_ADMIN      │  │ ip (VARCHAR)│  │ respuesta(LONGTEXT
        │ - ROLE_USER       │  │ fecha (DATE)│  │ tool_calls(JSONL) │
        │                   │  │ hora (TIME) │  │ tokens_used (INT) │
        │ FK: USUARIOS      │  │ descripcion │  │ response_time_ms  │
        │                   │  │ (VARCHAR)   │  │ created_at (TS)   │
        │ Índices:          │  │             │  │ deleted_at (TS)   │
        │   usuario_id      │  │ FK: USUARIOS│  │ [soft delete]      │
        └───────────────────┘  │             │  │                    │
                               │ Índices:    │  │ FK: USUARIOS      │
                               │   usuario_id│  │                    │
                               │   fecha     │  │ Índices:           │
                               └─────────────┘  │ usuario_id,        │
                                                │ created_at DESC    │
                                                └────────────────────┘
```

<div class="page-break"></div>

```
┌─────────────────────────────────────────────────────────┐
│                 DISPOSITIVOS                            │
├─────────────────────────────────────────────────────────┤
│ PK: id (BIGINT)                                         │
│ ip (VARCHAR 255, UNIQUE, NOT NULL)                      │
│ mac (VARCHAR 17, nullable)                              │
│ estado_autorizacion (VARCHAR 20)                        │
│   - AUTORIZADO                                          │
│   - NO_AUTORIZADO                                       │
│   - DESCONOCIDO                                         │
│ ultima_conexion (DATETIME, nullable)                    │
│                                                          │
│ Relación: Validación contra MACS_AUTORIZADAS            │
│ Índices: UK_ip                                          │
└──────────────────────────┬───────────────────────────────┘
                           │ Verifica contra
                           ▼
        ┌──────────────────────────────────────┐
        │    MACS_AUTORIZADAS                  │
        ├──────────────────────────────────────┤
        │ PK: id (BIGINT)                      │
        │ mac (VARCHAR 17, UNIQUE, NOT NULL)   │
        │ descripcion (VARCHAR 255, nullable)  │
        │ fecha_registro (DATETIME)            │
        │                                      │
        │ Índices: UK_mac                      │
        └──────────────────────────────────────┘
```

<div class="page-break"></div>

```textplain
┌─────────────────────────────────────────────────────────┐
│                  QOS_METRICS                            │
├─────────────────────────────────────────────────────────┤
│ PK: id (BIGINT)                                         │
│ timestamp (DATETIME, NOT NULL)                          │
│ target_ip (VARCHAR 255, NOT NULL)                       │
│ latency_ms (DOUBLE) - Latencia en milisegundos          │
│ jitter_ms (DOUBLE) - Variación de latencia              │
│ packet_loss_percent (DOUBLE) - Pérdida de paquetes      │
│ packets_sent (INT) - Paquetes transmitidos              │
│ packets_received (INT) - Paquetes recibidos             │
│ packets_lost (INT) - Paquetes perdidos                  │
│ mos_score (DOUBLE, 1.0-5.0) - Mean Opinion Score        │
│   1.0-2.0: BAD | 2.0-3.0: POOR | 3.0-3.5: FAIR        │
│   3.5-4.0: GOOD | 4.0-5.0: EXCELLENT                   │
│ rtt_min_ms, rtt_avg_ms, rtt_max_ms (DOUBLE)            │
│ protocol_type (VARCHAR) - ICMP, TCP, UDP, VOIP         │
│ quality_status (VARCHAR) - EXCELLENT, GOOD, FAIR,      │
│                            POOR, BAD                     │
│ during_active_call (TINYINT BOOLEAN)                   │
│                                                          │
│ Índices: target_ip, timestamp                           │
└─────────────────────────────────────────────────────────┘
```

### Relaciones Principales

| Relación                        | Tipo          | Descripción                               | Acciones       |
| ------------------------------- | ------------- | ----------------------------------------- | -------------- |
| USUARIOS ↔ ROLES                | 1:N           | Un usuario tiene múltiples roles          | CASCADE DELETE |
| USUARIOS ↔ EVENTOS              | 1:N           | Un usuario tiene múltiples eventos        | CASCADE DELETE |
| USUARIOS ↔ CONVERSACIONES       | 1:N           | Un usuario tiene múltiples conversaciones | CASCADE DELETE |
| DISPOSITIVOS → MACS_AUTORIZADAS | Validación    | Verificación de MAC en whitelist          | -              |
| QOS_METRICS → (ninguna)         | Independiente | Timestamps para correlación               | -              |

### Características de Integridad

- **Primary Keys:** IDENTITY (AUTO_INCREMENT)
- **Unique Constraints:** username, email (usuarios), ip (dispositivos), mac (macs_autorizadas)
- **Foreign Keys:** Todas con ON DELETE CASCADE
- **Soft Deletes:** Conversaciones mediante columna `deleted_at` (IS NULL en queries)
- **Timestamps:** Creación automática con @CreationTimestamp (Hibernate)
- **Índices Compuestos:** usuario_id + created_at (conversaciones) para performance

<div class="page-break"></div>

## Repositorio GitHub

**Enlace oficial:** <https://github.com/maidyperezb/shieldnet>

**Descripción:** Repositorio que contiene el código fuente completo del proyecto ShieldNet, incluyendo backend (Spring Boot), frontend (Thymeleaf + JavaScript), configuración, documentación técnica y schema de base de datos.

**Rama principal:**

- `main` - Rama con versiones estables

<div class="page-break"></div>

## Referencias y Anexos

### Anexo A: Matriz de Trazabilidad (Requisito → Evidencia)

| #   | Requisito del Enunciado                           | Evidencia (Archivo/Clase/Sección)                                                                                                                  | Verificación                               |
| --- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| 1   | **Stack/Ambiente Tecnológico**                    | Sección 2, pom.xml, application.properties                                                                                                         | ✓                                          |
| 1.1 | Backend (Spring Boot 3.4.4, Java 17)              | [pom.xml](../pom.xml) líneas 1-30                                                                                                                  | ✓                                          |
| 1.2 | BD (MySQL 8, JPA, Hibernate)                      | [pom.xml](../pom.xml) spring-boot-starter-data-jpa, mysql-connector-j                                                                              | ✓                                          |
| 1.3 | Seguridad (Spring Security 6.x)                   | [pom.xml](../pom.xml), [SecurityConfig.java](src/main/java/com/example/security/config/SecurityConfig.java)                                        | ✓                                          |
| 1.4 | Frontend (Thymeleaf 3.1+, Bootstrap 5.3)          | home.html, admin.html, user.html                                                                                                                   | ✓                                          |
| 1.5 | IA/LLM (Spring AI 1.0.0-M2, Groq API)             | [pom.xml](../pom.xml) spring-ai-core, spring-ai-openai-spring-boot-starter                                                                         | ✓                                          |
| 1.6 | Herramientas (pcap4j, asterisk-java)              | [pom.xml](../pom.xml) pcap4j 1.8.2, asterisk-java 3.39.0                                                                                           | ✓                                          |
| 1.7 | Variables de entorno                              | [application.properties](src/main/resources/application.properties), [application-chat.properties](src/main/resources/application-chat.properties) | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 2   | **Arquitectura por Capas**                        | Sección 3 (Arquitectura por Capas)                                                                                                                 | ✓                                          |
| 2.1 | Diagrama de 3 capas (Presentation, Logic, Data)   | Sección 3, diagrama ASCII                                                                                                                          | ✓                                          |
| 2.2 | Flujo de extremo a extremo                        | Sección 3, "Flujo de Extremo a Extremo"                                                                                                            | ✓                                          |
| 2.3 | Spring Security Filter Chain                      | [SecurityConfig.java](src/main/java/com/example/security/config/SecurityConfig.java)                                                               | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 3   | **Explicación de Cada Capa**                      | Sección 4 (Explicación de Cada Capa)                                                                                                               | ✓                                          |
| 3.1 | Capa Presentación (Responsabilidad + tecnologías) | Sección 4.1 + templates/ + static/                                                                                                                 | ✓                                          |
| 3.2 | Capa Lógica (11 Controllers)                      | Sección 4.2 + [ChatController](src/main/java/com/example/security/controller/ChatController.java)                                                  | ✓                                          |
| 3.3 | Capa Negocio (15 Services)                        | Sección 4.3 + [ChatService](src/main/java/com/example/security/servicios/ChatService.java)                                                         | ✓                                          |
| 3.4 | Capa Datos (6 Repositories)                       | Sección 4.4 + [ConversacionRepository](src/main/java/com/example/security/repositorio/ConversacionRepository.java)                                 | ✓                                          |
| 3.5 | Capa Persistencia (Entidades + BD)                | Sección 4.5 + [Conversacion.java](src/main/java/com/example/security/modelo/Conversacion.java)                                                     | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 4   | **Estructura del Proyecto Spring Boot**           | Sección 5 (Estructura del Proyecto)                                                                                                                | ✓                                          |
| 4.1 | Árbol de directorios completo                     | Sección 5, estructura de carpetas                                                                                                                  | ✓                                          |
| 4.2 | Paquetes Java y responsabilidades                 | Sección 5, "Paquetes Java - Responsabilidades"                                                                                                     | ✓                                          |
| 4.3 | Clases representativas por capa                   | Sección 5 + código fuente                                                                                                                          | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 5   | **Vistas e Interfaz de Usuario**                  | Sección 6 (Vistas e Interfaz de Usuario)                                                                                                           | ✓                                          |
| 5.1 | Páginas principales (home, login, admin, user)    | Sección 6.1 + [home.html](src/main/resources/templates/home.html), [admin.html](src/main/resources/templates/admin.html)                           | ✓                                          |
| 5.2 | Chat widget flotante                              | Sección 6.2 + [chat-widget.html](src/main/resources/templates/fragments/chat-widget.html)                                                          | ✓                                          |
| 5.3 | JavaScript client (SSE, fetch, rendering)         | Sección 6.3 + [chat.js](src/main/resources/static/js/chat.js)                                                                                      | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 6   | **Integración con LLM (Groq API)**                | Sección 7 (Integración con LLM)                                                                                                                    | ✓                                          |
| 6.1 | Configuración Spring AI + Groq                    | Sección 7.1 + [application-chat.properties](src/main/resources/application-chat.properties)                                                        | ✓                                          |
| 6.2 | Endpoints REST del chat                           | Sección 7.2 + [ChatController](src/main/java/com/example/security/controller/ChatController.java)                                                  | ✓                                          |
| 6.3 | ChatService (orquestación)                        | Sección 7.3 + [ChatService.java](src/main/java/com/example/security/servicios/ChatService.java)                                                    | ✓                                          |
| 6.4 | ContextBuilder (construcción de contexto)         | Sección 7.4 + [ContextBuilder.java](src/main/java/com/example/security/servicios/ContextBuilder.java)                                              | ✓                                          |
| 6.5 | ChatAIService (integración + tools)               | Sección 7.5 + [ChatAIService.java](src/main/java/com/example/security/servicios/ChatAIService.java)                                                | ✓                                          |
| 6.6 | System prompt y comportamiento del chatbot        | Sección 7.6 + [system-prompt.txt](src/main/resources/prompts/system-prompt.txt)                                                                    | ✓                                          |
| 6.7 | Persistencia de conversaciones                    | Sección 7.7 + [Conversacion.java](src/main/java/com/example/security/modelo/Conversacion.java)                                                     | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 7   | **Diagrama de Modelo de Datos**                   | Sección 8 (Diagrama de Modelo de Datos)                                                                                                            | ✓                                          |
| 7.1 | Diagrama E-R con todas las tablas                 | Sección 8.1 (diagrama ASCII)                                                                                                                       | ✓                                          |
| 7.2 | Descripción de cada tabla                         | Sección 8.2 + entidades JPA                                                                                                                        | ✓                                          |
| 7.3 | Relaciones 1:N y constraints                      | Sección 8.3                                                                                                                                        | ✓                                          |
| 7.4 | Integridad de datos e índices                     | Sección 8.4                                                                                                                                        | ✓                                          |
| 7.5 | Nota sobre discrepancia de schema                 | Sección 8.5 + documentación de hallazgo                                                                                                            | ✓                                          |
|     |                                                   |                                                                                                                                                    |                                            |
| 8   | **Link Repositorio GitHub**                       | Sección 9 + [github-link.txt](../docs/github-link.txt)                                                                                             | ✓ <https://github.com/maidyperezb/shieldnet> |

### Anexo B: Cobertura de Requisitos

**Resumen de cobertura:**

- ✓ **Stack y ambiente tecnológico:** 100% - Todas las tecnologías documentadas con versiones
- ✓ **Arquitectura por capas:** 100% - Diagrama de 3 capas + flujo completo explicado
- ✓ **Explicación de cada capa:** 100% - 5 capas (Presentation, Logic, Business, Data, Persistence) con detalles
- ✓ **Estructura del proyecto Spring Boot:** 100% - Árbol de directorios + paquetes + responsabilidades
- ✓ **Vistas e interfaz:** 100% - Páginas HTML + chat widget + JavaScript client
- ✓ **Integración con LLM:** 100% - Configuración + endpoints + servicios + context + system prompt + persistencia
- ✓ **Diagrama de modelo de datos:** 100% - Diagrama E-R + descripción de tablas + relaciones + integridad
- ✓ **Link repositorio GitHub:** 100% - URL oficial documentado

---

### Anexo C: Notas de Implementación

**Nota 1 - Function Calling Deshabilitado:**
Los 5 tools (queryEventHistory, queryQoSMetrics, listAuthorizedDevices, executeNetworkScan, generateMonitoringReport) están completamente implementados como @Bean methods en ChatAIService, pero la configuración actual establece `chat.enable-function-calling=false`(configurable mediante variable de entorno). Esto significa:

- Los tools existen y pueden usarse
- Spring AI no los invoca automáticamente en respuestas
- Cambiar la propiedad a `true` habilitaría esta funcionalidad

**Nota 2 - Autenticación y Autorización:**

- Todas las rutas excepto `/login`, `/registrar`, `/home`, `/api/dispositivos` requieren autenticación
- Roles implementados: ROLE_ADMIN (acceso completo) y ROLE_USER (acceso limitado)
- Contraseñas se hashean con BCryptPasswordEncoder
- CSRF está deshabilitado (válido para APIs REST)
- Las sesiones se gestionan mediante JSESSIONID (cookies de sesión)

**Nota 3 - Limitaciones del Chat:**

- Temperatura: Fija en 0.7 (configurable en properties)
- Máximo tokens: 2048 por respuesta (límite de Groq API)
- Timeout: 30 segundos para respuestas
- Rate limiting: 30 requests/minuto por usuario (configurable)
- Retención de datos: 90 días (configurables, luego soft delete automático)

<div class="page-break"></div>

## Conclusión

**ShieldNet** es una aplicación web integral para gestión de seguridad de red que demuestra:

1. **Arquitectura moderna:** Capas bien definidas con separación de responsabilidades
2. **Integración de IA:** Uso eficiente de Spring AI para conectar con Groq API
3. **Seguridad:** Autenticación, autorización y manejo seguro de datos
4. **Persistencia:** Diseño de base de datos relacional normalizado con integridad referencial
5. **UX/Frontend:** Interfaz responsiva con chat widget interactivo
6. **Documentación:** Código autodocumentado con comentarios técnicos detallados
