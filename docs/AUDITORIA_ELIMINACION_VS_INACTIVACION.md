# Auditoría: Eliminación vs. Inactivación de Datos (Frontend → Persistencia)

**Fecha:** 28/10/2026
**Alcance:** Todas las acciones iniciadas desde el frontend (plantillas Thymeleaf + `static/js`) que terminan en alguna operación de borrado sobre datos persistidos, rastreadas hasta la consulta SQL/ORM final.
**Restricciones respetadas:** no se modificó código ni esquemas; no se ejecutaron operaciones destructivas ni se eliminaron datos durante la investigación.

---

## 1. Resumen ejecutivo

El proyecto tiene **exactamente 3 endpoints de borrado** en el backend (verificado por búsqueda exhaustiva de `@DeleteMapping`, `deleteById`, `.delete(`, `deleteAll`, `DELETE FROM` y `@Modifying` en todo `src/main/java`), y el frontend emite **exactamente 3 peticiones HTTP `DELETE`** (verificado en todas las plantillas y el JS propio; no hay axios/XHR/sendBeacon).

| # | Funcionalidad | Comportamiento real | Clasificación |
|---|---|---|---|
| 1 | Eliminar usuario (`admin.html` → `DELETE /api/usuarios/{id}`) | **Borrado físico** (`DELETE FROM usuarios`) | 🔴 **INCORRECTO** |
| 2 | Eliminar MAC autorizada (`admin.html` → `DELETE /api/macs/{id}`) | **Borrado físico** (`DELETE FROM macs_autorizadas`) | 🔴 **INCORRECTO** |
| 3 | Limpiar historial de chat (widget → `DELETE /api/chat/history`) | **Soft delete** (`UPDATE ... SET deleted_at`) | 🟢 **CORRECTO** |

Hallazgos adicionales sin acción de borrado de registros: Dispositivos, Eventos/Bitácora, QoS, VoIP, "Reiniciar contador"/"Detener monitoreo" (operan sobre contadores en memoria, no sobre registros persistidos) — todos **CORRECTOS**.

**Agravante de seguridad (hallazgo 1):** el borrado físico de un usuario probablemente **falla en runtime** con violación de clave foránea para cualquier usuario con historial (`conversaciones.usuario_id` es `NOT NULL` sin cascada), y el controlador lo traduce en un **404 engañoso** ("Usuario no encontrado"). Para usuarios sin referencias, el borrado sí destruye la cuenta y sus roles. En ambos casos el comportamiento viola la regla de negocio.

**Agravante de seguridad (hallazgo 2):** al agregar una MAC se marcan sus dispositivos `AUTORIZADO`, pero al eliminarla **no se revierte** el estado: los dispositivos quedan marcados como autorizados de forma obsoleta. Un borrado lógico (inactivación) además permitiría reactivar la MAC y corregir este estado de forma consistente.

**Patrón existente a reutilizar:** el módulo de chat ya implementa exactamente el comportamiento que pide la regla de negocio (`Conversacion.deletedAt` + consultas que filtran `deleted_at IS NULL`). Es el modelo a seguir para usuarios y MACs.

---

## 2. Módulos y funcionalidades auditados

| Módulo | Pantalla / frontend | Backend | Persistencia |
|---|---|---|---|
| Usuarios | `admin.html` (tabla de usuarios, modal de borrado) | `UsuarioController`, `UserService` | `UsuarioRepository` → tabla `usuarios` (+ `roles`) |
| MACs autorizadas | `admin.html` (lista de MACs) | `MacAutorizadaController`, `MacAutorizadaService` | `MacAutorizadaRepository` → tabla `macs_autorizadas` |
| Chat (historial) | `fragments/chat-widget.html` + `static/js/chat.js` | `ChatController`, `ChatService` | `ConversacionRepository` → tabla `conversaciones` |
| Dispositivos | `admin.html`, `user.html` (solo lectura) | `DispositivoController`, `DispositivoService`, `ArpScannerService` | `DispositivoRepository` → tabla `dispositivos` |
| Eventos / Bitácora | `admin.html` (logs de actividad) | `EventoController`, `EventoService` | `EventoRepository` → tabla `eventos` |
| QoS | `admin.html`, `user.html` | `QoSController`, `QoSService` | `QoSMetricsRepository` |
| Tráfico de red | `admin.html`, `user.html` | `NetworkTrafficController`, `NetworkTrafficService` | Contadores en memoria + SSE |
| VoIP | `admin.html` | `AsteriskController`, `AsteriskAMIService` | Asterisk (externo, sin borrados propios) |
| Login / Registro | `login.html`, `registrar.html` | `AuthController`, `CustomUserDetailsService`, `UserService` | `UsuarioRepository` |

---

## 3. Inventario de acciones de eliminación e inactivación

### 3.1 Acciones de eliminación (frontend → persistencia)

| # | UI (archivo:línea) | Etiqueta / confirmación | Handler JS | Petición | Endpoint backend | Servicio | Mecanismo final |
|---|---|---|---|---|---|---|---|
| 1 | Botón papelera `admin.html:2697` (en `renderUserTable()`); modal `admin.html:1130-1141`; 2.º botón muerto `admin.html:3515` | Modal: «¿Está seguro que desea eliminar el usuario *{nombre}*?», botón **Eliminar** | `confirmDeleteUser()` `admin.html:4362-4393` | `fetch('/api/usuarios/'+id, {method:'DELETE'})` `admin.html:4370` | `DELETE /api/usuarios/{id}` — `UsuarioController.java:51-69` | `UserService.delete()` `UserService.java:175-179` | `usuarioRepository.delete(usuario)` → **`DELETE FROM usuarios`** |
| 2 | Botón papelera `admin.html:3251` (icono, sin etiqueta ni `title`) | `confirm()` nativo: «¿Eliminar esta MAC de la lista de autorizadas?» `admin.html:3295` | `eliminarMacAutorizada(id)` `admin.html:3293-3304` | `fetch('/api/macs/'+id, {method:'DELETE'})` `admin.html:3297` | `DELETE /api/macs/{id}` — `MacAutorizadaController.java:63-68` | `MacAutorizadaService.eliminarMac()` `MacAutorizadaService.java:33-35` | `macAutorizadaRepository.deleteById(id)` → **`DELETE FROM macs_autorizadas`** |
| 3 | Botón «Limpiar historial» `chat-widget.html:28` | `confirm()` nativo: «¿Deseas limpiar todo el historial de conversación?» `chat.js:169` | `clearHistory()` `chat.js:168-186` | `fetch('/api/chat/history', {method:'DELETE'})` `chat.js:173-176` | `DELETE /api/chat/history` — `ChatController.java:129-139` | `ChatService.limpiarHistorial()` `ChatService.java:140-143` | `softDeleteByUsuarioId()` → **`UPDATE conversaciones SET deleted_at = now() WHERE usuario_id = ? AND deleted_at IS NULL`** (`ConversacionRepository.java:35-38`) |

### 3.2 Acciones contiguas sin borrado de registros (verificadas)

| Acción | UI | Backend | Efecto real |
|---|---|---|---|
| Reiniciar contador | `admin.html:1450-1452`, `user.html:1031-1033` | `POST /api/network/reset` → `NetworkTrafficService.resetCounter()` (`NetworkTrafficService.java:254-255, 569-573`) | Pone a cero un `AtomicLong totalBytes` **en memoria**; no toca BD |
| Detener/Iniciar monitoreo | `admin.html:1453-1458`, `user.html:1025-1030` | `POST /api/network/stop|start` (`NetworkTrafficController.java:104-123`) | Controla la captura SSE; no borra datos |
| Limpiar filtros / cerrar alertas / vaciar tablas antes de re-render | `admin.html:2637-2652`, `admin.html:4646-4654`, `user.html:1268-1269`, etc. | — | Solo DOM/local; no hay `localStorage`/`sessionStorage` con datos de registros |

### 3.3 Inactivación

- **No existe ninguna acción de inactivar/desactivar** para registros en toda la UI (búsqueda de `inactivar|desactivar|toggle|switch|habilitar|quitar|revoke` sin resultados).
- Único concepto de estado existente: `Dispositivo.estadoAutorizacion` (`AUTORIZADO`/`NO_AUTORIZADO`/`DESCONOCIDO`, `Dispositivo.java:36-42`), **de solo lectura** en la UI; y badges de estado en línea de extensiones VoIP (`admin.html:2311-2318`).
- Único soft delete existente: `Conversacion.deletedAt` (`Conversacion.java:45-46, 134-136`).

---

## 4. Hallazgos detallados

### Hallazgo 1 — Eliminación física de usuarios 🔴 INCORRECTO

**Trazabilidad completa:**
1. **UI:** `admin.html:2697` — botón papelera en cada fila de `renderUserTable()`.
2. **Confirmación:** modal `admin.html:1130-1141` («¿Está seguro que desea eliminar el usuario X?») → `confirmDeleteUser()` `admin.html:4362`.
3. **Petición:** `fetch('/api/usuarios/'+userId, {method:'DELETE'})` (`admin.html:4370`). Éxito → recarga la tabla y registra actividad vía `POST /api/events/registre`.
4. **Endpoint:** `UsuarioController.deleteUsuario()` (`UsuarioController.java:51-69`).
5. **Servicio:** `UserService.delete(Long id)` (`UserService.java:175-179`).
6. **Persistencia:** `usuarioRepository.delete(usuario)` → JPA emite **`DELETE FROM usuarios WHERE id = ?`**. Borrado físico, sin campo `estado`/`activo`/`deleted_at` en `Usuario.java` (entidad sin ningún indicador de vigencia).

**Evidencia de impacto en relaciones e historial:**
- `Evento.usuario` (`Evento.java:40-42`, FK `usuario_id` nullable) y `Conversacion.usuario` (`Conversacion.java:22-24`, FK **`nullable = false`**) referencian al usuario. No hay `cascade` configurado y `ddl-auto=update` (`application.properties:7`) genera FKs sin `ON DELETE`.
- **Consecuencia A (usuarios con historial):** el `DELETE FROM` falla con `DataIntegrityViolationException` (violación de FK). `deleteUsuario()` captura `RuntimeException` y devuelve **404 «Usuario no encontrado con ID»**, mensaje engañoso que oculta el error real.
- **Consecuencia B (usuarios sin referencias):** el borrado tiene éxito y destruye la cuenta y sus roles (Hibernate borra físicamente las filas de la tabla `roles` de la `@ElementCollection`). Se pierde la identidad del autor para toda futura auditoría, y el username/email quedan libres para un nuevo registro (única restricción `unique` en `Usuario.java:26,37-38`), permitiendo que alguien más se registre con la identidad del usuario borrado.
- El evento «Eliminación de usuario: X» queda en la bitácora, pero sin usuario asociado al eliminado (asociado al admin que ejecuta la acción), y solo si el borrado no falla.

**¿Debería ser inactivación?** Sí. No existe campo de estado en `Usuario` que lo permita hoy.

**Efecto del cambio:** conserva cuentas e historial; requiere bloquear el login de inactivos (`CustomUserDetailsService`), filtrar el listado de administración, decidir el comportamiento de `unique` en `username`/`email` (recomendado: mantenerlas, la identidad no se libera) y mostrar el estado en la tabla de usuarios.

---

### Hallazgo 2 — Eliminación física de MACs autorizadas 🔴 INCORRECTO

**Trazabilidad completa:**
1. **UI:** `admin.html:3251` — botón papelera por fila en `loadMacsAutorizadas()` (icono solo, sin `title`).
2. **Confirmación:** `confirm()` nativo en `eliminarMacAutorizada()` (`admin.html:3295`).
3. **Petición:** `fetch('/api/macs/'+id, {method:'DELETE'})` (`admin.html:3297`); sin verificación de `response.ok` (los errores solo van a `console.error`).
4. **Endpoint:** `MacAutorizadaController.eliminar()` (`MacAutorizadaController.java:63-68`).
5. **Servicio:** `MacAutorizadaService.eliminarMac()` (`MacAutorizadaService.java:33-35`).
6. **Persistencia:** `macAutorizadaRepository.deleteById(id)` → **`DELETE FROM macs_autorizadas WHERE id = ?`**. `MacAutorizada` no tiene campo de estado/activo (solo `id`, `mac`, `descripcion`, `fechaRegistro`).

**Evidencia de impacto:**
- Se destruye `fechaRegistro`, la prueba de que la MAC estuvo autorizada y cuándo.
- **Asimetría de estado de seguridad:** al *agregar* una MAC, el controlador marca sus dispositivos `AUTORIZADO` (`MacAutorizadaController.java:49-53`); al *eliminarla*, **no se revierte nada**. Los dispositivos asociados quedan `AUTORIZADO` de forma obsoleta. El estado de autorización se calcula en cada escaneo ARP con `macAutorizadaService.esMacAutorizada()` (`ArpScannerService.java:195` → `MacAutorizadaService.java:20-23` → `existsByMac`), por lo que tras el borrado la MAC efectivamente deja de autorizarse, pero los registros de dispositivos conservan el estado anterior hasta el próximo escaneo.
- Re-agregar la misma MAC crea un **registro nuevo** con fecha de registro reciente, perdiendo la continuidad histórica (la deduplicación por `existsByMac` en `MacAutorizadaService.java:27-29` solo ve registros existentes; tras un soft delete tendría que filtrar por activos y **reactivar** el registro existente).

**¿Debería ser inactivación?** Sí. La MAC debe conservarse con su fecha original y un estado; además la inactivación habilita la reactivación y el revert coherente del estado de los dispositivos.

---

### Hallazgo 3 — Limpiar historial de chat 🟢 CORRECTO

- **UI:** botón «Limpiar historial» (`chat-widget.html:28`, visible en páginas de admin y usuario) → `confirm()` → `DELETE /api/chat/history` (`chat.js:168-176`).
- **Backend:** `ChatController.clearHistory()` (`ChatController.java:129-139`) → `ChatService.limpiarHistorial()` (`ChatService.java:140-143`) → `ConversacionRepository.softDeleteByUsuarioId()` (`ConversacionRepository.java:35-38`): `UPDATE conversaciones SET deleted_at = CURRENT_TIMESTAMP WHERE usuario_id = ? AND deleted_at IS NULL`.
- **Verificación:** el registro **se conserva** (`Conversacion.java:45-46,134-136`); todas las lecturas (`findByUsuarioIdOrderByCreatedAtDesc`, `findRecentConversations`, `countByUsuarioId`, `findByCreatedAtBefore`) filtran `deleted_at IS NULL` (`ConversacionRepository.java:23-30,43-50`). La limpieza programática de conversaciones antiguas también usa soft delete (`ChatService.java:148-156`).
- **Nota menor (cosmética, no incumple la regla):** los mensajes de log dicen «Historial eliminado» cuando en realidad se oculta; el modal/confirmación no aclara que el historial se conserva oculto para el usuario.

---

### Hallazgos adicionales 🟢 CORRECTOS (sin acción de borrado de registros)

| Módulo | Evidencia |
|---|---|
| Dispositivos | No existe ningún endpoint ni acción de borrado; `DispositivoService.saveDispositivo()` solo hace upsert por IP (`DispositivoService.java:26-37`) |
| Eventos / Bitácora | `EventoService` solo inserta (`EventoService.java:37-67`); sin `@DeleteMapping` en `EventoController` |
| QoS | Solo escritura/lectura de métricas; sin borrados |
| VoIP | Consultas a Asterisk; sin operaciones de borrado propias |
| Contadores de tráfico | `resetCounter()` opera sobre `AtomicLong` en memoria (`NetworkTrafficService.java:46,569-573`) |
| Chat (limpieza automática) | `limpiarConversacionesAntiguas()` usa soft delete (`ChatService.java:148-156`) |

### Código muerto detectado (informativo)

- `updateUserTable()` (`admin.html:3469-...`) contiene un **segundo botón de borrado de usuario** (`admin.html:3515`) pero la función no tiene sitios de llamada; junto con `updateLogsTable()` duplicado (`admin.html:2976` y `3546` — la segunda definición gana) es candidato a eliminación para evitar duplicidad futura.

---

## 5. Matriz de comportamiento actual vs. esperado

| # | Funcionalidad | ¿Permite eliminar hoy? | Operación real | ¿Backend elimina o cambia estado? | ¿Existe campo de estado? | ¿Debería inactivar? | Clasificación |
|---|---|---|---|---|---|---|---|
| 1 | Eliminar usuario | Sí («Eliminar») | `DELETE` → `repository.delete()` | **Elimina físicamente** (o falla por FK con error engañoso) | **No** | **Sí** | 🔴 INCORRECTO |
| 2 | Eliminar MAC autorizada | Sí (papelera) | `DELETE` → `deleteById()` | **Elimina físicamente** | **No** | **Sí** | 🔴 INCORRECTO |
| 3 | Limpiar historial de chat | Sí («Limpiar historial») | `DELETE` → `UPDATE deleted_at` | **Cambia estado (soft delete)** | Sí (`deleted_at`) | Ya lo hace | 🟢 CORRECTO |

No se identificaron casos de **RIESGO POTENCIAL** ni **REQUIERE VALIDACIÓN**: toda acción de borrado del frontend fue rastreada hasta su persistencia final con código verificado.

---

## 6. Correcciones recomendadas (plan por capas)

> No se aplicaron cambios: la instrucción de la auditoría exige documentar hallazgos y alcance antes de modificar código/esquemas. El plan reutiliza el patrón de soft delete ya existente en `Conversacion` y las verificaciones de estado existentes (`EstadoAutorizacion`) sin inventar mecanismos nuevos. Queda pendiente confirmación del usuario para implementar (implica cambios de comportamiento externo y un cambio de esquema).

### 6.1 Hallazgo 2 — MACs autorizadas (recomendado implementar primero: es el más acotado y añade valor de seguridad)

1. **Modelo** (`MacAutorizada.java`): añadir `@Column(name = "activo", nullable = false) private Boolean activo = true;` (o un campo `estado`; con `ddl-auto=update` la columna se crea con default para filas existentes — conviene `columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE"` para el backfill automático).
2. **Repositorio** (`MacAutorizadaRepository.java`): cambiar `existsByMac`/`findByMac` a `existsByMacAndActivoTrue(mac)` / `findByMacAndActivoTrue(mac)` **o** filtrar en el servicio, para que la autorización (`esMacAutorizada`, usada por `ArpScannerService.java:195`) solo considere MACs activas.
3. **Servicio** (`MacAutorizadaService.java`):
   - `agregarMac()`: si el registro existe pero está inactivo → **reactivarlo** (conservando `fechaRegistro`) en lugar de devolverlo tal cual.
   - Sustituir `eliminarMac(id)` por `desactivarMac(id)`: `activo = false` + `save()`. Mantener `eliminarMac` (deprecado) solo si se requiere compatibilidad temporal.
4. **Controlador** (`MacAutorizadaController.java:63-68`): cambiar `@DeleteMapping` por `@PatchMapping("/{id}/desactivar"` (o reutilizar `DELETE` con semántica de baja lógica, decidiendo con el equipo) y, **al desactivar, marcar los dispositivos con esa MAC como `NO_AUTORIZADO`**, reflejando el mismo pase de estado que ya existe al agregar (`:49-53`). Actualizar el mensaje del evento a «MAC autorizada desactivada».
5. **Consultas/listados** (`admin.html`): en `loadMacsAutorizadas()` (`admin.html:3229`) mostrar solo activas (o mostrar todas con badge y permitir reactivar); cambiar el botón de papelera (`admin.html:3251`) por «Desactivar» con etiqueta/`title`, mensaje de confirmación («La MAC se conservará como inactiva») y verificación de `response.ok` (hoy no la tiene).
6. **Integridad:** sin claves foráneas hacia `macs_autorizadas`, el cambio no afecta a otras tablas; `dispositivos` solo cambia de estado, nunca se borra.

### 6.2 Hallazgo 1 — Usuarios (mayor alcance: requiere decisión de negocio)

1. **Modelo** (`Usuario.java`): añadir campo `activo` (default `true`, backfill para filas existentes) — mismo patrón que 6.1.
2. **Servicio de autenticación** (`CustomUserDetailsService.java`): rechazar el login de usuarios inactivos (excepción «Usuario desactivado») — **obligatorio** para que la inactivación sea efectiva; si no, un usuario inactivado podría seguir entrando.
3. **Servicio** (`UserService.java:175-179`): sustituir `delete()` por `desactivarUsuario(id)` (`activo = false` + `save()`); `findAll()` debe poder filtrar por estado.
4. **Controlador** (`UsuarioController.java:51-69`): exponer la inactivación (recomendado `PATCH /api/usuarios/{id}/desactivar`; si se conserva `DELETE` con semántica lógica, ajustar respuestas) y registrar el evento «Usuario desactivado». Evaluando proteger el endpoint con autorización `ROLE_ADMIN` (verificar `SecurityConfig.java` antes).
5. **Frontend** (`admin.html`):
   - `confirmDeleteUser()` (`admin.html:4362-4393`): apuntar al nuevo endpoint; cambiar modal (`admin.html:1130-1141`) a «Desactivar Usuario» con mensaje «El usuario se conservará inactivo y su historial se mantendrá»; renombrar `openDeleteUserModal`/`deleteUserName` por equivalentes de desactivación.
   - `renderUserTable()` (`admin.html:2654`): badge de estado Activo/Inactivos, botón de desactivar/reactivar y filtro por estado.
   - Eliminar el código muerto `updateUserTable()` (`admin.html:3469+`, incluye el 2.º botón de borrado en `3515`).
6. **Restricciones `unique` en `username`/`email`:** se recomienda **mantenerlas** — la identidad del usuario desactivado no debe liberarse (evita suplantación y conserva la trazabilidad de `eventos.usuario_id` y `conversaciones.usuario_id`). Consecuencia aceptada y a comunicar: no se podrá crear otro usuario con el mismo username/email mientras el original esté inactivado. *(Decisión de negocio pendiente de confirmar.)*
7. **Consultas y relaciones:** `Eventos`, `Conversaciones` e historiales quedan intactos (la FK sigue apuntando a la fila conservada). Verificar que ContextBuilder y reportes del chat (`ContextBuilder.java`) no muestren usuarios inactivos como activos.

### 6.3 Hallazgo 3 — Chat (solo cosmético, opcional)

Ajustar textos de confirmación/log para reflejar que el historial se oculta y se conserva (`chat.js:169`, `ChatService.java:142`, `ChatController.java:136`).

---

## 7. Pruebas realizadas y resultados

**Ejecutadas durante la auditoría (análisis estático, sin datos ni entorno en ejecución):**

| Verificación | Resultado |
|---|---|
| Búsqueda exhaustiva de `@DeleteMapping`, `deleteById`, `.delete(`, `deleteAll`, `DELETE FROM`, `@Modifying` en `src/main/java` | Solo 3 endpoints de borrado: usuarios, macs, chat/history ✔ |
| Búsqueda de `DELETE`/`fetch`/`axios`/`XHR`/`sendBeacon` en todo el frontend | Solo 3 peticiones `DELETE`; coinciden 1:1 con los endpoints anteriores ✔ |
| Verificación del mecanismo real tras cada botón (no por el nombre) | Usuarios y MACs = borrado físico JPA; Chat = `UPDATE deleted_at` ✔ |
| Entidades sin campo de estado | `Usuario` y `MacAutorizada` no tienen indicador de vigencia; `Conversacion` sí ✔ |
| Configuración de esquema | `spring.jpa.hibernate.ddl-auto=update` (`application.properties:7`) — FKs sin `ON DELETE`, coherente con el fallo previsto en el borrado de usuarios ✔ |
| Efecto de `reset`/`stop` de tráfico | Contadores en memoria (`NetworkTrafficService.java:46,569-573`), sin borrado de registros ✔ |
| Existencia de acciones «inactivar/desactivar» previas | Ninguna en todo el proyecto ✔ |

**No ejecutadas (requieren entorno en ejecución y credenciales):** pruebas de integración contra MySQL que comprueben: (a) que tras inactivar una MAC/usuario el registro persiste y desaparece de los listados operativos; (b) que el login de un usuario inactivado es rechazado; (c) que el borrado físico de un usuario con historial devuelve hoy el error de FK enmascarado como 404; (d) que eventos y conversaciones del usuario conservado siguen siendo consultables. **Recomendación:** añadir tests de integración (Spring Boot Test + H2/MySQL de prueba) al implementar la corrección, y validar en entorno de desarrollo antes de desplegar.

---

## 8. Riesgos pendientes y limitaciones

1. **Decisión de negocio pendiente:** comportamiento de `username`/`email` únicos tras inactivar (recomendación: no liberar la identidad).
2. **Cambio de esquema:** añadir `activo` a `usuarios` y `macs_autorizadas` con `ddl-auto=update`; el backfill de filas existentes debe verificarse en entorno (las columnas nuevas con `NOT NULL DEFAULT TRUE` se rellenan automáticamente, pero conviene confirmarlo).
3. **Seguridad del endpoint de usuarios:** no se verificó en esta auditoría si `DELETE /api/usuarios/{id}` está restringido a `ROLE_ADMIN` en `SecurityConfig.java`; debe revisarse al implementar (un usuario no admin podría eliminar/desactivar cuentas si la ruta no está protegida).
4. **Duplicidad de JS en `admin.html`:** `updateLogsTable()` definida dos veces (`admin.html:2976` y `3546`) y `updateUserTable()` muerta; conviene limpiar para que futuras modificaciones no apliquen en la función equivocada.
5. **VoIP:** las extensiones/llamadas viven en Asterisk (externo); no se auditó su persistencia interna por estar fuera del repositorio.
6. **`shieldnet-ml/` y `Modelo Matematico/`:** servicios externos en Python; no contienen acciones de borrado de los módulos auditados (el frontend no les envía operaciones destructivas), pero no se auditaron en profundidad por estar fuera del alcance del flujo frontend→BD principal.

---

*Auditoría basada en lectura directa del código fuente completo del repositorio. Todas las afirmaciones cuentan con referencia `archivo:línea`; ninguna funcionalidad se declaró segura sin verificar su implementación.*
